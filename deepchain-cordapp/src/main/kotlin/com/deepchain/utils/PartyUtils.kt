package com.deepchain.utils

import com.deepchain.contracts.ParamContract
import com.deepchain.data.bigDecimalList2BigIntegerList
import com.deepchain.flows.param.CreateDecryptShareFlow
import com.deepchain.flows.param.PartyUploadFlow
import com.paillier.algorithm.Decrypt
import com.paillier.algorithm.Encrypt
import com.paillier.algorithm.ProofSystem
import com.paillier.entity.Ciphertext
import com.paillier.entity.PublicAndPrivateParams
import com.paillier.utils.PaillierUtils
import com.paillier.utils.WashVector
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import java.math.BigDecimal
import java.math.BigInteger

object PartyUtils {
    /**
     * 加密参数并将其发送给 miner
     * 如果 debug 为真则只加密上传一部分
     */
    fun encryptAndUpload(rpcOps: CordaRPCOps, groupId: String, filePath: String, debug: Boolean = true) {
        // prepare raw data
        val washVector = WashVector(filePath)
        val vectorList = washVector.wash(ALPHA_LEN)

        val (params, alphaList, gList) = getPaillierApi(rpcOps, groupId)
        val partyName = rpcOps.nodeInfo().legalIdentities.first().name

        // encrypt data
        for (idx in 0 until vectorList.size step SINGLE_STATE_PARAM_LEN) {
            if (debug && idx > DEBUG_PARAM_LEN) {
                break
            }

            val bigIntegerListList = mutableListOf<List<BigInteger>>()
            // 一个 flow 上传多个参数
            for (offset in 0 until SINGLE_STATE_PARAM_LEN) {
                if (idx + offset >= vectorList.size) break

                val preClearList = PaillierUtils.cleartextPreProcess(vectorList[idx + offset])

                // encryption
                val encParam = Encrypt.verifiableEncryptWithAlpha(preClearList, alphaList, gList, params)
                bigIntegerListList.add(DataUtils.cipherText2BigIntegerList(encParam))
            }

            // upload data
            val flowFuture = rpcOps.startFlow(PartyUploadFlow::Initiator,
                    bigIntegerListList, UniqueIdentifier.fromString(groupId),
                    vectorList.size, idx).returnValue

            try {
                flowFuture.getOrThrow()
            } catch (e: Exception) {
                println(e.message)
                return
            }
            println("[DEEPCHAIN] $partyName: Param $idx/${vectorList.size} commited successfully to ledger")
        }
        println("[DEEPCHAIN] Param upload finished!")
    }

    /**
     * 下载更新后的参数并且上传 share
     */
    fun downloadAndUploadShare(rpcOps: CordaRPCOps, groupId: String) {
        val (params, _, _) = getPaillierApi(rpcOps, groupId)

        var page = 1
        while (true) {
            val (ciphertextListOnePage, totalLenOffsetOnePage) =
                    downloadUpdatedSinglePage(rpcOps, groupId, page)
            if (ciphertextListOnePage.isEmpty()) break  // no more page
            for ((idx, ciphertextOneState) in ciphertextListOnePage.withIndex()) {
                val totalLen = totalLenOffsetOnePage[idx].first
                val offset = totalLenOffsetOnePage[idx].second
                val verifiableCiphertext = mutableListOf<List<BigInteger>>()
                for (ciphertext in ciphertextOneState) {
                    // including cipher, ci, cshareProof
                    verifiableCiphertext.add(DataUtils.shareCipher2BigIntegerList(
                            Decrypt.verifiableDecryptBySecretshare(
                                    ciphertext.cipher, 1, params.secretKey, params)))
                }

                uploadShare(rpcOps, groupId, totalLen, offset, verifiableCiphertext)
            }
            page++
        }

        val partyName = rpcOps.nodeInfo().legalIdentities.first().name
        println("[DEEPCHAIN] $partyName: Upload share successfully")
    }

    /**
     * 下载 share 并且解密
     * 如果 debug 为假则会按照参数的形式来组织解密后的数据
     */
    fun downloadShareAndDecrypt(rpcOps: CordaRPCOps, groupId: String, filePath: String = "",
                                debug: Boolean = true, iterationRound: Int = 1) {
        val (params, alphaList, _) = getPaillierApi(rpcOps, groupId)
        val partyNum = params.partyNum
        val groupState = BlockChainUtils.groupExists(groupId, rpcOps)
        val groupStateData = groupState.first().state.data
        val me = rpcOps.nodeInfo().legalIdentities.first()
        val groupMembers = groupStateData.groupMembers + groupStateData.owner

        var page = 1
        val vectorList = mutableListOf<List<BigDecimal>>()
        while (true) {
            val myShareStatesOnePage = VaultQueryUtils.queryParams(
                    rpcOps, groupId, page = page, party = me, stateType = "decryptshare")
            // 来自多个 party 的 cipher list list
            // 每个 cipher list list 装了一页的多个 cipher list
            // 每个 cipher list 装了一个 state 中的多个 cipher
            val verifiedShareListFromParties = mutableListOf<List<List<Ciphertext>>>()
            val myCipherList = getCipherListFromStates(myShareStatesOnePage, params)
            if (myShareStatesOnePage.isEmpty()) break  // no more pages
            for (otherParty in groupMembers) {
                if (otherParty == me) {
                    verifiedShareListFromParties.add(myCipherList)
                }
                else {
                    val otherShareStatesOnePage = VaultQueryUtils.queryParams(
                            rpcOps, groupId, page = page, party = otherParty, stateType = "decryptshare")
                    verifiedShareListFromParties.add(
                            getCipherListFromStates(otherShareStatesOnePage, params, true)
                    )
                }
            }

            for (i in 0 until myShareStatesOnePage.size) {  // 一个 page 的 state 数
                for (j in 0 until SINGLE_STATE_PARAM_LEN) {  // 一个 state 的 param 数
                    val cipherShareMap = mutableMapOf<Int, BigInteger>()
                    for (k in 0 until partyNum) {
                        println("i:$i, j:$j, k:$k")
                        cipherShareMap[k + 1] = verifiedShareListFromParties[k][i][j].cipherShares[1]!!
                    }
                    val returnClearText = Decrypt.decryptWithAlphaByCipherShares(cipherShareMap, alphaList, params)
                    val decimalClearText = PaillierUtils.cleartextProProcess(returnClearText, params)
                    vectorList.add(decimalClearText)
                }
            }
            page++
        }

        if (debug) {
            if (filePath != "") {
                WashVector.writeStringToFile(vectorList.toString(), filePath)
            }
        }
        else {
            val wv = WashVector(partyNum, iterationRound)
            if (filePath == "") throw PaillierException("File path is required to export data.")
            wv.unwash(vectorList, filePath)
        }
    }

    /**
     * 获取 official 传来的参数并还原成 Paillier api
     */
    private fun getPaillierApi(rpcOps: CordaRPCOps, groupId: String):
            Triple<PublicAndPrivateParams, List<BigInteger>, List<BigInteger>> {
        // 查询 api state
        val alphaAndGParamState = VaultQueryUtils.queryAlphaAndG(rpcOps, groupId).first()
        val stateDataA = alphaAndGParamState.state.data
        val alphaList = bigDecimalList2BigIntegerList(stateDataA.alphaList)
        val gList = bigDecimalList2BigIntegerList(stateDataA.gList)

        val publicAndPrivateParamsState = VaultQueryUtils.queryPublicAndSecret(rpcOps, groupId).first()
        val stateDataP = publicAndPrivateParamsState.state.data
        val params = DataUtils.restorePaillierParam(stateDataP)

        return Triple(params, alphaList, gList)
    }

    /**
     * 获取单页的更新后的参数
     */
    private fun downloadUpdatedSinglePage(rpcOps: CordaRPCOps, groupId: String, page: Int):
            Pair<List<List<Ciphertext>>, List<Pair<Int, Int>>> {
        val groupState = BlockChainUtils.groupExists(groupId, rpcOps)
        val groupStateData = groupState.first().state.data
        val miner = groupStateData.miner

        val updatedOnePage = VaultQueryUtils.queryParams(rpcOps, groupId, page = page, party = miner)
        if (updatedOnePage.isEmpty()) return Pair(listOf(), listOf()) // no more page

        // 每一页有多个 state
        val ciphertextListOnePage = mutableListOf<List<Ciphertext>>()
        val totalLenOffsetOnePage = mutableListOf<Pair<Int, Int>>()
        for (updatedState in updatedOnePage) {
            val updatedParamList = updatedState.state.data.param
            val ciphertexeList = mutableListOf<Ciphertext>()
            // 每个 state 有多个 cipher
            for (singleUpdatedParam in updatedParamList) {
                val ciphertext = DataUtils.bigIntegerList2cipherText(
                        bigDecimalList2BigIntegerList(singleUpdatedParam))
                ciphertexeList.add(ciphertext)
            }
            ciphertextListOnePage.add(ciphertexeList)
            totalLenOffsetOnePage.add(Pair(updatedState.state.data.totalLen,
                    updatedState.state.data.offset))
        }

        return Pair(ciphertextListOnePage, totalLenOffsetOnePage)
    }

    /**
     * 把 share 发送给其他的 party
     */
    private fun uploadShare(rpcOps: CordaRPCOps, groupId: String,
                            totalLen:Int, offset: Int,
                            flowParamListList: List<List<BigInteger>>) {

        val flowFuture = rpcOps.startFlow(CreateDecryptShareFlow::Initiator,
                flowParamListList,
                UniqueIdentifier.fromString(groupId),
                totalLen, offset
        ).returnValue

        try {
            flowFuture.getOrThrow()
            val partyName = rpcOps.nodeInfo().legalIdentities.first().name
            println("[DEEPCHAIN] $partyName: Share $offset/$totalLen updated successfully to ledger")
        } catch (e: Exception) {
            println(e.message)
            return
        }
    }

    /**
     * 从 state 中提取出 share
     */
    private fun getCipherListFromStates(shareStatesOnePage: List<StateAndRef<ParamContract.State>>,
                                        params: PublicAndPrivateParams, verify: Boolean = false):
            List<List<Ciphertext>> {
        val cipherShareOnePage = mutableListOf<List<Ciphertext>>()
        val proofSystem = ProofSystem(params.bitLength, params.n)
        for (shareStateOneState in shareStatesOnePage) {
            val cipherShareOneState = mutableListOf<Ciphertext>()
            val paramListOneState = shareStateOneState.state.data.param
            for (paramList in paramListOneState) {
                val cipherShare = DataUtils.bigIntegerList2cipherShare(
                        bigDecimalList2BigIntegerList(paramList))
                if (verify) {
                    val verifyCDResult = proofSystem.verifyCDProof(cipherShare.csharesProof[1])
                    if (!verifyCDResult) {
                        throw PaillierException("[DEEPCHAIN] Verify CD result failed")
                    }
                }
                cipherShareOneState.add(cipherShare)
            }
            cipherShareOnePage.add(cipherShareOneState)
        }
        return cipherShareOnePage
    }
}