package com.deepchain.flows.official

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.contracts.AlphaAndGParamContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.math.BigInteger

object SendAlphaAndGParamFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val alphaList: List<BigInteger>,
                    private val gList: List<BigInteger>,
                    private val groupId: UniqueIdentifier,
                    private val groupMembers: List<Party>): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {

            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val me = serviceHub.myInfo.legalIdentities.first()
            val builder = TransactionBuilder(notary)
            AlphaAndGParamContract.generateCreate(builder, me, alphaList, gList,
                    groupMembers, groupId, notary)

            val tx = serviceHub.signInitialTransaction(builder, me.owningKey)

            return subFlow(FinalityFlow(tx))
        }
    }
}