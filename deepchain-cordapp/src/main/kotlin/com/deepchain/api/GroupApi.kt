package com.deepchain.api

import com.deepchain.contracts.GroupContract
import com.deepchain.flows.group.ApplyGroupFlow
import com.deepchain.flows.group.CreateGroupFlow
import com.deepchain.utils.VaultQueryUtils
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("group")
class GroupApi(private val rpcOps: CordaRPCOps) {

    /**
     * Get all states from the node's vault.
     */
    @GET
    @Path("vault")
    @Produces(MediaType.APPLICATION_JSON)
    fun getVault(@DefaultValue("") @QueryParam("groupId") groupId: String,
                 @DefaultValue("1") @QueryParam("page") page: Int = 1):
            Pair<List<StateAndRef<GroupContract.State>>, List<StateAndRef<GroupContract.State>>> {
        val unconsumedStates = VaultQueryUtils.queryGroups(rpcOps, groupId, page)
        val consumedStates = VaultQueryUtils.queryGroups(rpcOps, groupId, page, true)
        return Pair(unconsumedStates, consumedStates)
    }

    /**
     * Create group
     */
    @POST
    @Path("create-group")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun createGroup(createGroupData: CreateGroupData): Response {
        val minerNode = rpcOps.networkMapSnapshot()
                .map { it.legalIdentities.first() }
                .filter { it.name.organisation == createGroupData.miner }.singleOrNull()
                ?: return Response.status(Response.Status.NOT_FOUND).entity(
                        "No miner found by the name of ${createGroupData.miner}") .build()

        val officialNode = rpcOps.networkMapSnapshot()
                .map { it.legalIdentities.first() }
                .filter { it.name.organisation == createGroupData.official }.singleOrNull()
                ?: return Response.status(Response.Status.NOT_FOUND).entity(
                        "No official found by the name of ${createGroupData.official}") .build()

        val flowFuture = rpcOps.startFlow(CreateGroupFlow::OwnerFlow, minerNode, officialNode,
                createGroupData.maxNum, createGroupData.description, createGroupData.untilTime).returnValue
        val result = try {
            flowFuture.getOrThrow()
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.message).build()
        }

        val linearId = (result.coreTransaction.outputStates.first() as GroupContract.State).linearId
        val message = "Added a new group report for linear id $linearId " +
                "in transaction ${result.tx.id} committed to ledger."

        return Response.accepted().entity(message).build()
    }

    /**
     * Join a group by its group id and its owner's name
     */
    @POST
    @Path("join-group")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun joinGroup(joinGroupData: JoinGroupData): Response {
        val groupId = UniqueIdentifier.fromString(joinGroupData.groupId)
        val ownerParty: Party = rpcOps.wellKnownPartyFromX500Name(joinGroupData.ownerParty)!!
        val flowFuture = rpcOps.startFlow(ApplyGroupFlow::Initiator, ownerParty, groupId).returnValue
        try {
            flowFuture.getOrThrow()
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.message).build()
        }

        val message = "Join group successfully report for state id $groupId."
        return Response.accepted().entity(message).build()
    }
}