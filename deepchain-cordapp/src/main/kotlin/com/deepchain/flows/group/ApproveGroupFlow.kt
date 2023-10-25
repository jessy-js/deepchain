package com.deepchain.flows.group

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.contracts.GroupContract
import com.deepchain.flows.DeepchainFlowException
import com.deepchain.flows.groupExists
import net.corda.core.contracts.TransactionResolutionException
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.utilities.unwrap

/**
 * Flow to approve the applicant's request.
 * Invoke a subflow to add the applicant to the group state.
 */
@InitiatedBy(ApplyGroupFlow.Initiator::class)
class ApproveGroupFlow(val ownerSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call(){
        val pairInfo = ownerSession.receive<Pair<UniqueIdentifier, Party>>().unwrap { it }
        val groupId = pairInfo.first
        val applicant = pairInfo.second

        val groupStateAndRefs = serviceHub.groupExists(groupId)
        if (groupStateAndRefs.isEmpty())
            throw DeepchainFlowException("Group does not exists.")

        val stateAndRef = try {
            serviceHub.toStateAndRef<GroupContract.State>(groupStateAndRefs.first().ref)
        } catch (e: TransactionResolutionException) {
            throw DeepchainFlowException("Group state could not be found.", e)
        }

        subFlow(UpdateGroupFlow.Initiator(applicant, stateAndRef))
    }
}