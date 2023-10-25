package com.deepchain.flows

import com.deepchain.contracts.GroupContract
import com.deepchain.contracts.ParamContract
import com.deepchain.data.StateType
import com.deepchain.schemas.GroupSchemaV1
import com.deepchain.schemas.ParamSchemaV1
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria

fun ServiceHub.groupExists(linearId: UniqueIdentifier): List<StateAndRef<GroupContract.State>> {
    val queryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
    val customCriteria =
            QueryCriteria.VaultCustomQueryCriteria(
                    GroupSchemaV1.PersistentGroupState::linearId.equal(linearId.toString()))

    val criteria = queryCriteria.and(customCriteria)

    val pages = vaultService.queryBy(GroupContract.State::class.java, criteria)
    return pages.states
}

fun ServiceHub.retrieveParamWithType(groupId: UniqueIdentifier, stateType: StateType
): List<StateAndRef<ParamContract.State>> {
    val queryCriteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)
    val customCriteria = QueryCriteria.VaultCustomQueryCriteria(
            ParamSchemaV1.PersistentParamState::groupId.equal(groupId.toString()))
    val customCriteriaWithType = QueryCriteria.VaultCustomQueryCriteria(
            ParamSchemaV1.PersistentParamState::stateType.equal(stateType.toString()))

    val criteria = queryCriteria.and(customCriteria).and(customCriteriaWithType)
    val pages = vaultService.queryBy(ParamContract.State::class.java, criteria)

    return pages.states
}

fun ServiceHub.retrieveParamWithPartyAndType(groupId: UniqueIdentifier, party: AbstractParty, stateType: StateType
): List<StateAndRef<ParamContract.State>> {
    val queryCriteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)
    val customCriteria = QueryCriteria.VaultCustomQueryCriteria(
            ParamSchemaV1.PersistentParamState::groupId.equal(groupId.toString()))
    val customCriteriaWithParty = QueryCriteria.VaultCustomQueryCriteria(
            ParamSchemaV1.PersistentParamState::owner.equal(party))
    val customCriteriaWithType = QueryCriteria.VaultCustomQueryCriteria(
            ParamSchemaV1.PersistentParamState::stateType.equal(stateType.toString()))

    val criteria = queryCriteria.and(customCriteria).and(customCriteriaWithParty).and(customCriteriaWithType)
    val pages = vaultService.queryBy(ParamContract.State::class.java, criteria)

    return pages.states
}
