package com.deepchain.utils

import com.deepchain.flows.official.SendAlphaAndGParamFlow
import com.deepchain.flows.official.SendPublicAndSecretParamsFlow
import com.paillier.algorithm.ParamsSetup
import com.paillier.entity.PublicAndPrivateParams
import com.paillier.utils.FileUtils
import com.paillier.utils.PaillierUtils
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import java.math.BigInteger

object OfficialUtils {
    /**
     * 生成 Paillier Api 的参数，并将其分发给各个 party
     */
    fun initiatePaillierApi(groupId: String, rpcOps: CordaRPCOps) {

        val linearId = UniqueIdentifier.fromString(groupId)
        val groupState = BlockChainUtils.groupExists(groupId, rpcOps)
        // 这里 group 默认是存在的
        val groupIdStateData = groupState.first().state.data
        val groupMembers = groupIdStateData.groupMembers + groupIdStateData.owner

        val savedBasePath = FileUtils.savedFileBasePath()
        println("[DEEPCHAIN] Official: Saved path: $savedBasePath")
        val partyNum = groupIdStateData.groupMembers.size + 1
        val identifier = "Old"

        // 1.初始化基本参数
        val params = generateBasicParams(savedBasePath, partyNum, identifier)

        // 2.将基本参数分别分发给各个 party（调用 flow）
        sendBasicParams(rpcOps, params, linearId, groupMembers, groupIdStateData.miner)

        // 3. 初始化 AplhaList，GList
        val (alphaSplitList, gSplitList) = generateAlphaAndGParams(savedBasePath, partyNum,
                identifier, params)

        // 4.将 AplhaList，GList 分发给各个 party（调用flow）
        sendAlphaAndGParams(rpcOps, alphaSplitList, gSplitList, linearId, groupMembers)
    }

    /**
     * 把已经提交的 tx 号展示出来
     * 每个字段最多展示一页的 tx（默认为 10 个）
     */
    fun showPublishedTxs(rpcOps: CordaRPCOps, groupId: String): Map<String, List<String>> {
        val allTxIdsMap = mutableMapOf<String, List<String>>()
        for (stateType in VAULT_ORDER) {
            allTxIdsMap[stateType] = VaultQueryUtils.getTxIdsByStateType(rpcOps, groupId, stateType)
        }

        return allTxIdsMap
    }

    /**
     * 生成基本参数
     */
    private fun generateBasicParams(savedBasePath: String, partyNum: Int,
                                    identifier: String): PublicAndPrivateParams {
        var params: PublicAndPrivateParams
        if (!FileUtils.isFileExists(savedBasePath, "publicParams", partyNum, ALPHA_LEN, identifier) &&
                !FileUtils.isFileExists(savedBasePath, "secretParams", partyNum, ALPHA_LEN, identifier)) {
            params = ParamsSetup.generateParams()
            params = ParamsSetup.splitSecretKey(params, partyNum)

            FileUtils.savePublicParams(params, savedBasePath, ALPHA_LEN, identifier)
            FileUtils.saveSecretParams(params, savedBasePath, ALPHA_LEN, identifier)
            println("[DEEPCHAIN] Official: Public and secret params saved!")
        }
        else {
            params = FileUtils.loadParams(savedBasePath, partyNum, ALPHA_LEN, identifier)
            println("[DEEPCHAIN] Official: Public and secret params loaded!")
        }
        return params
    }

    /**
     * 生成 alpha g
     */
    private fun generateAlphaAndGParams(savedBasePath: String, partyNum: Int,
                                        identifier: String,
                                        params: PublicAndPrivateParams):
            Pair<List<BigInteger>, List<BigInteger>> {
        val alphaSplitList: List<BigInteger>
        val gSplitList: List<BigInteger>
        if (!FileUtils.isFileExists(savedBasePath, "alphaSplitList", partyNum, ALPHA_LEN, identifier) &&
                !FileUtils.isFileExists(savedBasePath, "gSplitList", partyNum, ALPHA_LEN, identifier)) {
            alphaSplitList = PaillierUtils.generateAlphaList(params, ALPHA_LEN)
            gSplitList = PaillierUtils.calculateGList(alphaSplitList, params)
            FileUtils.saveAlphaParams(alphaSplitList, savedBasePath, partyNum, ALPHA_LEN, identifier)
            FileUtils.saveGParams(gSplitList, savedBasePath, partyNum, ALPHA_LEN, identifier)
            println("[DEEPCHAIN] Official: Alpha and g list saved!")
        } else {
            alphaSplitList = FileUtils.loadAlphaList(savedBasePath, partyNum, ALPHA_LEN, identifier)
            gSplitList = FileUtils.loadgSplitList(savedBasePath, partyNum, ALPHA_LEN, identifier)
            println("[DEEPCHAIN] Official: Alpha and g list loaded!")
        }
        return Pair(alphaSplitList, gSplitList)
    }

    /**
     * 发送基本参数给各个 party
     */
    private fun sendBasicParams(rpcOps: CordaRPCOps, params: PublicAndPrivateParams,
                                linearId: UniqueIdentifier, groupMembers: List<Party>,
                                miner: Party) {
        // 参数压缩到一个 list 里面
        val paramList = mutableListOf<BigInteger>()
        paramList.add(params.n)
        paramList.add(params.nsquare)
        paramList.add(params.g)
        paramList.add(params.delta)
        paramList.add(params.theta)
        paramList.add(params.bitLength.toBigInteger())
        paramList.add(params.partyNum.toBigInteger())
        val secretKeyMap = params.secretShare

        // upload to parties
        for (sk in secretKeyMap) {
            val flowFuture = rpcOps.startFlow(SendPublicAndSecretParamsFlow::Initiator,
                    paramList, sk.value, linearId, groupMembers[sk.key - 1]).returnValue
            try {
                flowFuture.getOrThrow()
            } catch (e: Exception) {
                println(e.message)
                return
            }
        }

        // upload to miners
        // 给 miner 的 state 里面 sk 为 0
        val flowFuture = rpcOps.startFlow(SendPublicAndSecretParamsFlow::Initiator,
                paramList, BigInteger.ZERO, linearId, miner).returnValue
        try {
            flowFuture.getOrThrow()
        } catch (e: Exception) {
            println(e.message)
            return
        }
    }

    /**
     * 发送 alpha 和 g 给各个 party
     */
    private fun sendAlphaAndGParams(rpcOps: CordaRPCOps, alphaSplitList: List<BigInteger>,
                                    gSplitList: List<BigInteger>, linearId: UniqueIdentifier,
                                    groupMembers: List<Party>) {
        val flowFuture = rpcOps.startFlow(SendAlphaAndGParamFlow::Initiator,
                alphaSplitList, gSplitList, linearId, groupMembers).returnValue

        try {
            flowFuture.getOrThrow()
        } catch (e: Exception) {
            println(e.message)
            return
        }
    }
}
