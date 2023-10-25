package com.deepchain.api

import com.deepchain.contracts.AlphaAndGParamContract
import com.deepchain.contracts.PublicAndPaivateParamsContract
import com.deepchain.utils.OfficialUtils
import com.deepchain.utils.VaultQueryUtils
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("official")
class OfficialApi(private val rpcOps: CordaRPCOps) {

    @GET
    @Path("init")
    @Produces(MediaType.APPLICATION_JSON)
    fun initPaillierApi(@DefaultValue("") @QueryParam("groupId") groupId: String,
                        @DefaultValue("1") @QueryParam("page") page: Int = 1):
            Pair<List<StateAndRef<AlphaAndGParamContract.State>>,
            List<StateAndRef<AlphaAndGParamContract.State>>> {
        OfficialUtils.initiatePaillierApi(groupId, rpcOps)

        val unconsumedStates = VaultQueryUtils.queryAlphaAndG(rpcOps, groupId, page)
        val consumedStates = VaultQueryUtils.queryAlphaAndG(rpcOps, groupId, page, true)
        return Pair(unconsumedStates, consumedStates)
    }

    @GET
    @Path("vault")
    @Produces(MediaType.APPLICATION_JSON)
    fun getVault(@DefaultValue("") @QueryParam("groupId") groupId: String,
                 @DefaultValue("1") @QueryParam("page") page: Int = 1):
            Pair<List<StateAndRef<AlphaAndGParamContract.State>>,
            List<StateAndRef<AlphaAndGParamContract.State>>> {
        val unconsumedStates = VaultQueryUtils.queryAlphaAndG(rpcOps, groupId, page)
        val consumedStates = VaultQueryUtils.queryAlphaAndG(rpcOps, groupId, page, true)
        return Pair(unconsumedStates, consumedStates)
    }

    @GET
    @Path("vault-test")
    @Produces(MediaType.APPLICATION_JSON)
    fun getVaultTest():
            Pair<List<StateAndRef<AlphaAndGParamContract.State>>,
                    List<StateAndRef<AlphaAndGParamContract.State>>> {
        val unconsumedStates = rpcOps.vaultQuery(AlphaAndGParamContract.State::class.java).states
        val consumedStates = rpcOps.vaultQueryByCriteria(
                QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.CONSUMED),
                AlphaAndGParamContract.State::class.java).states
        return Pair(unconsumedStates, consumedStates)
    }

    @GET
    @Path("vault-basic")
    @Produces(MediaType.APPLICATION_JSON)
    fun getVaultBasic(@DefaultValue("") @QueryParam("groupId") groupId: String,
                      @DefaultValue("1") @QueryParam("page") page: Int = 1):
            Pair<List<StateAndRef<PublicAndPaivateParamsContract.State>>,
            List<StateAndRef<PublicAndPaivateParamsContract.State>>> {
        val unconsumedStates = VaultQueryUtils.queryPublicAndSecret(rpcOps, groupId, page)
        val consumedStates = VaultQueryUtils.queryPublicAndSecret(rpcOps, groupId, page, true)
        return Pair(unconsumedStates, consumedStates)
    }

    @GET
    @Path("all-vault")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllVault(@QueryParam("groupId") groupId: String): Map<String, List<String>> {
        return OfficialUtils.showPublishedTxs(rpcOps, groupId)
    }
}