package com.deepchain.flows.group

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.flows.DeepchainFlowException
import com.deepchain.flows.groupExists
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party

/**
 * Flow to join a group by the group owner's name and group id.
 * This flow sends a message to the owner to get his permission.
 */
object ApplyGroupFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val owner: Party,
                    private val groupId: UniqueIdentifier): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {

            val groupStateAndRefs = serviceHub.groupExists(groupId)
            if (groupStateAndRefs.isNotEmpty())
                throw DeepchainFlowException("Already in this group.")

            val ownerSession = initiateFlow(owner)
            val me = serviceHub.myInfo.legalIdentities.first()

            ownerSession.send(Pair(groupId, me))
        }
    }
}
