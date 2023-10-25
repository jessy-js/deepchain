package com.deepchain.flows.group

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.contracts.GroupContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

/**
 * Flow to add the applicant to the group state.
 */
object UpdateGroupFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val newGroupMember: Party,
                    private val groupStateAndRef: StateAndRef<GroupContract.State>
    ): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val me = serviceHub.myInfo.legalIdentities.first()
            val builder = TransactionBuilder(notary)
            GroupContract.generateUpdate(builder, groupStateAndRef, newGroupMember, notary)

            val tx = serviceHub.signInitialTransaction(builder, me.owningKey)
            val ownerSession = initiateFlow(newGroupMember)
            val ownerSignature = subFlow(CollectSignatureFlow(tx, ownerSession, newGroupMember.owningKey))
            val signedTx = tx + ownerSignature

            return subFlow(FinalityFlow(signedTx))
        }
    }

    @InitiatedBy(UpdateGroupFlow.Initiator::class)
    class ApplicantFlow(val applicantSession: FlowSession) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(applicantSession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val states: Iterable<GroupContract.State> = stx.tx.outputs.map { it.data as GroupContract.State }
                    states.forEach { state ->
                        state.participants.forEach { anon ->
                            require(serviceHub.identityService.wellKnownPartyFromAnonymous(anon) != null) {
                                "Transaction state $state involves unknown participant $anon"
                            }
                        }
                    }
                }
            }

            val txId = subFlow(signTransactionFlow).id

            return waitForLedgerCommit(txId)
        }

    }
}
