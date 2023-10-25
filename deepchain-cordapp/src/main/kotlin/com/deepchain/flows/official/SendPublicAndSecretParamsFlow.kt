package com.deepchain.flows.official

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.contracts.PublicAndPaivateParamsContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.math.BigInteger

object SendPublicAndSecretParamsFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val paramList: List<BigInteger>,
                    private val secretKey: BigInteger,
                    private val groupId: UniqueIdentifier,
                    private val receiver: Party): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {

            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val me = serviceHub.myInfo.legalIdentities.first()
            val builder = TransactionBuilder(notary)
            PublicAndPaivateParamsContract.generateCreate(builder, me, paramList, secretKey,
                    receiver, groupId, notary)

            val tx = serviceHub.signInitialTransaction(builder, me.owningKey)

            return subFlow(FinalityFlow(tx))
        }
    }
}