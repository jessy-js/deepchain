package com.deepchain.utils

import com.deepchain.contracts.AlphaAndGParamContract
import com.deepchain.contracts.GroupContract
import com.deepchain.contracts.ParamContract
import com.deepchain.contracts.PublicAndPaivateParamsContract
import com.deepchain.data.StateType
import com.deepchain.schemas.AlphaAndGParamSchemaV1
import com.deepchain.schemas.GroupSchemaV1
import com.deepchain.schemas.ParamSchemaV1
import com.deepchain.schemas.PublicAndPrivateParamsSchemaV1
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria

object VaultQueryUtils {
    fun queryParams(rpcOps: CordaRPCOps, groupId: String = "", stateType: String = "param",
                    page: Int = 1, isConsumed: Boolean = false, pageSize: Int = DEFAULT_PAGE_NUM,
                    party: Party ?= null, offset: Int = 0):
            List<StateAndRef<ParamContract.State>> {
        // according to consumed or not
        val queryCriteria = if (isConsumed) QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.CONSUMED)
        else QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)

        // according to groupId
        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(
                ParamSchemaV1.PersistentParamState::groupId.equal(groupId))

        val customCriteriaWithParty = QueryCriteria.VaultCustomQueryCriteria(
                ParamSchemaV1.PersistentParamState::owner.equal(party))

        // according to type
        val sType = if (stateType == "param") StateType.PARAM else StateType.DECRYPTSHARE
        val customCriteriaWithType = QueryCriteria.VaultCustomQueryCriteria(
                ParamSchemaV1.PersistentParamState::stateType.equal(sType.toString()))

        // according tp offset
        val customCriteriaWithOffset = QueryCriteria.VaultCustomQueryCriteria(
                ParamSchemaV1.PersistentParamState::offset_num.equal(offset))

        // 要把关于 consumed 放在后面，不然可能会不生效
        var criteria = if (groupId != "") customCriteria.and(customCriteriaWithType).and(queryCriteria)
        else customCriteriaWithType.and(queryCriteria)
        if (party != null) {
            criteria = criteria.and(customCriteriaWithParty)
        }
        if (offset != 0) {
            criteria = criteria.and(customCriteriaWithOffset)
        }

        val pages = rpcOps.vaultQueryByWithPagingSpec(ParamContract.State::class.java, criteria,
                PageSpecification(pageNumber = page, pageSize = pageSize))

        return pages.states
    }

    fun queryGroups(rpcOps: CordaRPCOps, groupId: String = "",
                    page: Int = 1, isConsumed: Boolean = false):
            List<StateAndRef<GroupContract.State>> {
        // according to consumed or not
        val queryCriteria = if (isConsumed) QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.CONSUMED)
        else QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)

        // according to groupId
        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(
                GroupSchemaV1.PersistentGroupState::linearId.equal(groupId))

        val criteria = if (groupId != "") customCriteria.and(queryCriteria)
        else queryCriteria

        val pages = rpcOps.vaultQueryByWithPagingSpec(GroupContract.State::class.java, criteria,
                PageSpecification(pageNumber = page, pageSize = DEFAULT_PAGE_NUM))

        return pages.states
    }

    fun queryAlphaAndG(rpcOps: CordaRPCOps, groupId: String = "",
                       page: Int = 1, isConsumed: Boolean = false):
            List<StateAndRef<AlphaAndGParamContract.State>> {

        val queryCriteria = if (isConsumed) QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.CONSUMED)
        else QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)

        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(
                AlphaAndGParamSchemaV1.PersistentAlphaAndGParamState::groupId.equal(groupId))

        val criteria = if (groupId != "") customCriteria.and(queryCriteria)
        else queryCriteria

        val pages = rpcOps.vaultQueryByWithPagingSpec(AlphaAndGParamContract.State::class.java, criteria,
                PageSpecification(pageNumber = page, pageSize = DEFAULT_PAGE_NUM))

        return pages.states
    }

    fun queryPublicAndSecret(rpcOps: CordaRPCOps, groupId: String = "",
                                        page: Int = 1, isConsumed: Boolean = false):
            List<StateAndRef<PublicAndPaivateParamsContract.State>> {
        val queryCriteria = if (isConsumed) QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.CONSUMED)
        else QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)

        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(
                PublicAndPrivateParamsSchemaV1.PersistentPublicAndPrivateParamsState::groupId.equal(
                        groupId))

        val criteria = if (groupId != "") customCriteria.and(queryCriteria)
        else queryCriteria

        val pages = rpcOps.vaultQueryByWithPagingSpec(PublicAndPaivateParamsContract.State::class.java, criteria,
                PageSpecification(pageNumber = page, pageSize = DEFAULT_PAGE_NUM))

        return pages.states
    }

    fun getTxIdsByStateType(rpcOps: CordaRPCOps, groupId: String, stateType: String,
                            page: Int = 1, isConsumed: Boolean = false): List<String> {

        when (stateType) {
            "group" -> {
                val states = queryGroups(rpcOps, groupId, page, isConsumed)
                val txIdList = mutableListOf<String>()
                for (state in states) {
                    txIdList.add(state.ref.txhash.toString())
                }
                return txIdList
            }
            in setOf("param", "decryptshare") -> {
                val states = queryParams(rpcOps, groupId, stateType, page, isConsumed)
                val txIdList = mutableListOf<String>()
                for (state in states) {
                    txIdList.add(state.ref.txhash.toString())
                }
                return txIdList
            }
            "alpha" -> {
                val states = queryAlphaAndG(rpcOps, groupId, page, isConsumed)
                val txIdList = mutableListOf<String>()
                for (state in states) {
                    txIdList.add(state.ref.txhash.toString())
                }
                return txIdList
            }
            "public" -> {
                val states = queryPublicAndSecret(rpcOps, groupId, page, isConsumed)
                val txIdList = mutableListOf<String>()
                for (state in states) {
                    txIdList.add(state.ref.txhash.toString())
                }
                return txIdList
            }
            else -> return listOf()
        }
    }
}

