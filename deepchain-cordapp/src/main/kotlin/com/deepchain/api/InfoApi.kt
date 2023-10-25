package com.deepchain.api

import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/**
 * Get basic node info
 */
@Path("info")
class InfoApi(val rpcOps: CordaRPCOps) {
    private val selfIdentity = rpcOps.nodeInfo().legalIdentities.first()
    private val myLegalName = selfIdentity.name
    private val SERVICE_NODE_NAME = CordaX500Name("Notary", "Guangzhou", "CN")

    /**
     * Returns the node's name.
     */
    @GET
    @Path("identity")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("identity" to myLegalName.organisation)

    /**
     * Returns all parties registered with the network map.
     */
    @GET
    @Path("participants")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = rpcOps.networkMapSnapshot()
        return mapOf("participants" to nodeInfo
                .map { it.legalIdentities.first().name }
                .filter { it !in listOf(myLegalName, SERVICE_NODE_NAME) })
    }
}