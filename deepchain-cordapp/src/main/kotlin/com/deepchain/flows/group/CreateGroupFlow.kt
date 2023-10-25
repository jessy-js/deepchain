package com.deepchain.flows.group

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.contracts.GroupContract
import net.corda.core.contracts.TimeWindow
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

/**
 * Flow to create a group state.
 * This flow creates a group state that are visible to the owner and miner.
 */
object CreateGroupFlow {

    @InitiatingFlow
    @StartableByRPC
    class OwnerFlow(private val miner: Party,
                    private val official: Party,
                    private val maxNum: Int,
                    private val description: String,
                    private val untilTime: Instant) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val me = serviceHub.myInfo.legalIdentities.first()
            val builder = TransactionBuilder(notary)
            GroupContract.generateCreate(builder, me, miner, official, maxNum,
                    description, TimeWindow.untilOnly(untilTime), notary)

            val tx = serviceHub.signInitialTransaction(builder, me.owningKey)
            val minerSession = initiateFlow(miner)
            val minerSignature = subFlow(CollectSignatureFlow(tx, minerSession, miner.owningKey))

            val officialSession = initiateFlow(official)
            val officialSignature = subFlow(CollectSignatureFlow(tx, officialSession, official.owningKey))
            val signedTx = tx + officialSignature + minerSignature

            return subFlow(FinalityFlow(signedTx))
        }

    }

    @InitiatedBy(CreateGroupFlow.OwnerFlow::class)
    class MinerFlow(val ownerSession: FlowSession) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(ownerSession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val states: Iterable<GroupContract.State> = stx.tx.outputs.map { it.data as GroupContract.State }
                    states.forEach { state ->
                        state.participants.forEach { anon ->
                            require(serviceHub.identityService.wellKnownPartyFromAnonymous(anon) != null) {
                                "Transaction state $state involves unknown participant $anon"
                            }
                        }
                        require(state.miner in serviceHub.myInfo.legalIdentities ||
                                state.official in serviceHub.myInfo.legalIdentities) {
                            "Incorrect miner or official on transaction state $state"
                        }
                    }
                }
            }

            val txId = subFlow(signTransactionFlow).id

            return waitForLedgerCommit(txId)
        }

    }
}
