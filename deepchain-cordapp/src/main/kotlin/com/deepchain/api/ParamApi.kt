package com.deepchain.api

import com.deepchain.contracts.ParamContract
import com.deepchain.flows.param.CreateDecryptShareFlow
import com.deepchain.flows.param.PartyUploadFlow
import com.deepchain.utils.MinerUtils
import com.deepchain.utils.PartyUtils
import com.deepchain.utils.VaultQueryUtils
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("param")
class ParamApi(private val rpcOps: CordaRPCOps) {
    /**
     * Get all states from the node's vault.
     * Can also retrieve with some constrains
     */
    @GET
    @Path("vault")
    @Produces(MediaType.APPLICATION_JSON)
    fun getVault(@DefaultValue("") @QueryParam("groupId") groupId: String,
                 @DefaultValue("1") @QueryParam("page") page: Int,
                 @DefaultValue("10") @QueryParam("pageSize") pageSize: Int):
            Pair<List<StateAndRef<ParamContract.State>>, List<StateAndRef<ParamContract.State>>> {
        val unconsumedStates = VaultQueryUtils.queryParams(rpcOps, groupId, page = page, pageSize = pageSize)
        val consumedStates = VaultQueryUtils.queryParams(
                rpcOps, groupId, page =  page, pageSize = pageSize, isConsumed = true)
        return Pair(unconsumedStates, consumedStates)
    }

    /**
     * Send param to the designated miner.
     */
    @POST
    @Path("send-param")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun sendParam(sendParamData: SendParamData): Response {
        val groupId = UniqueIdentifier.fromString(sendParamData.groupId)

        val flowFuture = rpcOps.startFlow(PartyUploadFlow::Initiator, sendParamData.param,
                groupId, sendParamData.totalLen, sendParamData.offset).returnValue
        try {
            flowFuture.getOrThrow()
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.message).build()
        }

        val message = "Sent a new param successfully."

        return Response.accepted().entity(message).build()
    }

    /**
     * Read the designated file, encrypt the data and upload them to ledger
     */
    @GET
    @Path("encrypt-upload")
    @Produces(MediaType.APPLICATION_JSON)
    fun encryptUpload(@QueryParam("groupId") groupId: String, @QueryParam("filePath") filePath: String):
            Pair<List<StateAndRef<ParamContract.State>>,
                    List<StateAndRef<ParamContract.State>>> {
        PartyUtils.encryptAndUpload(rpcOps, groupId, filePath)

        val unconsumedStates = VaultQueryUtils.queryParams(rpcOps, groupId, "param")
        val consumedStates = VaultQueryUtils.queryParams(
                rpcOps, groupId, "param", isConsumed = true)
        return Pair(unconsumedStates, consumedStates)
    }

    @GET
    @Path("download-update")
    @Produces(MediaType.APPLICATION_JSON)
    fun downloadUpdate(@QueryParam("groupId") groupId: String):
            Pair<List<StateAndRef<ParamContract.State>>,
                    List<StateAndRef<ParamContract.State>>> {
        MinerUtils.minerUpdate(rpcOps, groupId)

        val unconsumedStates = VaultQueryUtils.queryParams(rpcOps, groupId, "param")
        val consumedStates = VaultQueryUtils.queryParams(
                rpcOps, groupId, "param", isConsumed = true)
        return Pair(unconsumedStates, consumedStates)
    }

    @GET
    @Path("upload-share")
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadShare(@QueryParam("groupId") groupId: String):
            Pair<List<StateAndRef<ParamContract.State>>,
                    List<StateAndRef<ParamContract.State>>> {
        PartyUtils.downloadAndUploadShare(rpcOps, groupId)

        val unconsumedStates = VaultQueryUtils.queryParams(rpcOps, groupId, "decryptshare")
        val consumedStates = VaultQueryUtils.queryParams(
                rpcOps, groupId, "decryptshare", isConsumed = true)
        return Pair(unconsumedStates, consumedStates)
    }

    /**
     * Send decrypt to the parties in the same group.
     */
    @POST
    @Path("decrypt-share")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun decryptShare(sendParamData: SendParamData): Response {
        val groupId = UniqueIdentifier.fromString(sendParamData.groupId)

        val flowFuture = rpcOps.startFlow(CreateDecryptShareFlow::Initiator, sendParamData.param,
                groupId, sendParamData.totalLen, sendParamData.offset).returnValue
        try {
            flowFuture.getOrThrow()
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.message).build()
        }

        val message = "Share successfully."

        return Response.accepted().entity(message).build()
    }

    @GET
    @Path("decrypt")
    @Produces(MediaType.APPLICATION_JSON)
    fun decrypt(@QueryParam("groupId") groupId: String,
                @DefaultValue("") @QueryParam("filePath") filePath: String): Response {
        PartyUtils.downloadShareAndDecrypt(rpcOps, groupId, filePath)

        val message = "Decrypt successfully."

        return Response.accepted().entity(message).build()
    }
}