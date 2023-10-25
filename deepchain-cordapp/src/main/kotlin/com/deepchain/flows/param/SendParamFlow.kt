package com.deepchain.flows.param

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.contracts.ParamContract
import com.deepchain.data.StateType
import com.deepchain.flows.groupExists
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.math.BigInteger

/**
 * Flow to send param to the designated receivers.
 * This flow creates a param state that are visible to the owner and receivers.
 */
object SendParamFlow {

    @InitiatingFlow
    @StartableByRPC
    class SenderFlow(private val param: List<List<BigInteger>>,
                     private val stateType: StateType,
                     private val groupId: UniqueIdentifier,
                     private val totalLen: Int,
                     private val offset: Int,
                     private val receiverList: List<Party>,
                     private val stateAndRefList: List<StateAndRef<ParamContract.State>> = listOf()):
            FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val groupStateAndRefs = serviceHub.groupExists(groupId)
            val miner = groupStateAndRefs.first().state.data.miner
            val official = groupStateAndRefs.first().state.data.official
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val me = serviceHub.myInfo.legalIdentities.first()
            val builder = TransactionBuilder(notary)
            if (miner != me) {
                ParamContract.generateCreateParam(builder, me, official, receiverList, stateType,
                        param, groupId, totalLen, offset, notary)
            }
            else {
                ParamContract.generateMinerUpdate(builder, me, official, receiverList, stateType,
                        param, groupId, totalLen, offset, notary, stateAndRefList)
            }


            val tx = serviceHub.signInitialTransaction(builder, me.owningKey)
            var signedTx = tx
            for (receiver in receiverList) {
                val receiverSession = initiateFlow(receiver)
                val receiverSignature = subFlow(CollectSignatureFlow(tx, receiverSession, receiver.owningKey))
                signedTx += receiverSignature
            }

            return subFlow(FinalityFlow(signedTx))
        }

    }

    @InitiatedBy(SendParamFlow.SenderFlow::class)
    class ReceiverFlow(val receiverSession: FlowSession) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(receiverSession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val states: Iterable<ParamContract.State> = stx.tx.outputs.map { it.data as ParamContract.State }
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