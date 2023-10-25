package com.deepchain.utils

import com.deepchain.contracts.GroupContract
import com.deepchain.schemas.GroupSchemaV1
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort

object BlockChainUtils {
    fun getrpcOps(host: String, port: Int, username: String, password: String): CordaRPCOps {
        val client = CordaRPCClient(NetworkHostAndPort(host, port))
        val connection = client.start(username, password)
        return connection.proxy
    }

    fun groupExists(groupId: String, rpcOps: CordaRPCOps): List<StateAndRef<GroupContract.State>> {
        val linearId = UniqueIdentifier.fromString(groupId)
        val queryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
        val customCriteria =
                QueryCriteria.VaultCustomQueryCriteria(
                        GroupSchemaV1.PersistentGroupState::linearId.equal(linearId.toString()))

        val criteria = queryCriteria.and(customCriteria)
        return rpcOps.vaultQueryByCriteria(criteria, GroupContract.State::class.java).states
    }
}