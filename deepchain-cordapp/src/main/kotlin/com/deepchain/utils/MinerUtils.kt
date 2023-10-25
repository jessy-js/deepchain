package com.deepchain.utils

import com.deepchain.contracts.ParamContract
import com.deepchain.data.bigDecimalListList2BigIntegerListList
import com.deepchain.flows.param.MinerUpdateFlowAuto
import com.paillier.algorithm.Mine
import com.paillier.algorithm.ProofSystem
import com.paillier.entity.Ciphertext
import com.paillier.entity.PublicAndPrivateParams
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import java.math.BigInteger

object MinerUtils {
    /**
     * miner 将来自 party 的数据整合之后传回各个 party
     */
    fun minerUpdate(rpcOps: CordaRPCOps, groupId: String) {
        val groupState = BlockChainUtils.groupExists(groupId, rpcOps)
        val groupStateData = groupState.first().state.data

        val groupMembers = groupStateData.groupMembers + groupStateData.owner
        val publicParam = VaultQueryUtils.queryPublicAndSecret(rpcOps, groupId)
        val params = DataUtils.restorePaillierParam(publicParam.first().state.data)

        while (true) {
            val (ciphertextOnePageFromParties, paramStatesOnePageFromParties) =
                    downloadDataSinglePage(rpcOps, groupId, groupMembers)
            if (ciphertextOnePageFromParties.isEmpty()) break  // 已经没有新的一页了

            minerUpdateSinglePage(rpcOps, groupId,
                    ciphertextOnePageFromParties,
                    paramStatesOnePageFromParties, params)
        }
    }

    /**
     * 下载数据
     */
    private fun downloadDataSinglePage(rpcOps: CordaRPCOps, groupId: String, groupMembers: List<Party>):
            Pair<List<List<List<Ciphertext>>>, List<List<StateAndRef<ParamContract.State>>>> {
        // 存放包含所有 party 在这一页的数据
        // 单个 party 在这一页中有若干个 state
        // 每个 state 中的数据包括若干个 cipher
        val ciphertextOnePageFromParties = mutableListOf<List<List<Ciphertext>>>()
        val paramStatesOnePageFromParties = mutableListOf<List<StateAndRef<ParamContract.State>>>()
        // 这里是假设每个 party 的 state 的 offset 都是从小到大的顺序来上传的
        for (party in groupMembers) {
            // 一页的 state，可能有若干个
            val paramStateOnePage = VaultQueryUtils.queryParams(rpcOps, groupId, party = party)
            if (paramStateOnePage.isEmpty()) {  // no more page
                return Pair(listOf(), listOf())
            }
            paramStatesOnePageFromParties.add(paramStateOnePage)

            // 一页页来处理
            // 存放单个 party 在这一页中的若干个 state 对应的数据
            // 其中每个 state 中的数据包括若干个 cipher
            val ciphertextOnePageOneParty = mutableListOf<List<Ciphertext>>()
            for (paramState in paramStateOnePage) {
                val paramDataTemp = paramState.state.data.param  // type is List<List<BigDecimal>>
                val paramDataListList = bigDecimalListList2BigIntegerListList(paramDataTemp)
                // 存放单个 state 里面的若干个 cipher
                val cipherList = mutableListOf<Ciphertext>()
                // 每一个 state 里面有多个 cipher
                // 存放时 cipher 储存为 List<BigInteger>
                // 即每一个 List<BigInteger> 可以转换成一个 cipher
                for (paramDataList in paramDataListList) {
                    val ciphertext = DataUtils.bigIntegerList2cipherText(paramDataList)
                    cipherList.add(ciphertext)
                }
                ciphertextOnePageOneParty.add(cipherList)
            }
            ciphertextOnePageFromParties.add(ciphertextOnePageOneParty)
        }
        return Pair(ciphertextOnePageFromParties, paramStatesOnePageFromParties)
    }

    /**
     * 将对应的数据进行整合
     */
    private fun minerUpdateSinglePage(rpcOps: CordaRPCOps, groupId: String,
                              ciphertextOnePageFromParties: List<List<List<Ciphertext>>>,
                              paramStatesOnePageFromParties: List<List<StateAndRef<ParamContract.State>>>,
                              params: PublicAndPrivateParams) {
        val proofSystem = ProofSystem(params.bitLength, params.n)
        val partyNum = params.partyNum
        val numPerPage = ciphertextOnePageFromParties.first().size
        for (j in 0 until numPerPage) {  // 依次处理一页的每个 state
            // 一个 updated state 需要接受来自多个 party 的同一个位置的 state 作为输入
            val flowInputStates = mutableListOf<StateAndRef<ParamContract.State>>()
            // 每个 state 的数据也同样包含多个 cipher
            val flowParamListList = mutableListOf<List<BigInteger>>()
            val numPerState = SINGLE_STATE_PARAM_LEN
            for (k in 0 until numPerState) {  // 依次处理每个 state 的多个 cipher
                val cListFromParties = mutableListOf<Ciphertext>()  // 给 miner 处理的数据单位
                for (i in 0 until partyNum) {  // 来自不同 party 的单个 cipher 放到一起给 miner 处理
                    val ciphertext = ciphertextOnePageFromParties[i][j][k]
                    // 验证加密数据的合法性
                    val verifyPKResult = proofSystem.verifyPKProof(ciphertext.proofPKEntity, ciphertext.cipher)
                    if (!verifyPKResult) {
                        throw PaillierException("PK Proof verification fails.")
                    }
                    cListFromParties.add(ciphertext)
                }

                // 返回的也是单个 cipher
                val updatedCiphertext = DataUtils.cipherText2BigIntegerList(
                        Mine.mine(cListFromParties, params))
                // 把多个 cipher 凑到一起放到一个 state 里面上传
                flowParamListList.add(updatedCiphertext)
            }

            for (i in 0 until partyNum) {
                flowInputStates.add(paramStatesOnePageFromParties[i][j])
            }

            // upload
            uploadData(rpcOps, groupId, flowInputStates, flowParamListList)
        }
    }

    /**
     * 上传更新后的参数
     */
    private fun uploadData(rpcOps: CordaRPCOps, groupId: String,
                           flowInputStates: List<StateAndRef<ParamContract.State>>,
                           flowParamListList: List<List<BigInteger>>) {
        val offset = flowInputStates.first().state.data.offset
        val totalLen = flowInputStates.first().state.data.totalLen
        val flowFuture = rpcOps.startFlow(MinerUpdateFlowAuto::Initiator,
                flowParamListList,
                UniqueIdentifier.fromString(groupId),
                totalLen, offset, flowInputStates
        ).returnValue

        try {
            flowFuture.getOrThrow()
            println("[DEEPCHAIN] Param $offset/$totalLen updated successfully to ledger")
        } catch (e: Exception) {
            println(e.message)
            return
        }
    }
}