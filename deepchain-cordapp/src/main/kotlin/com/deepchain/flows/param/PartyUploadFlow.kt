package com.deepchain.flows.param

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.data.StateType
import com.deepchain.flows.DeepchainFlowException
import com.deepchain.flows.groupExists
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import java.math.BigInteger

/**
 * Flow to send encrypted param to the designated miner.
 * This flow creates a param state that are visible to the owner and miner.
 */
object PartyUploadFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val param: List<List<BigInteger>>,
                    private val groupId: UniqueIdentifier,
                    private val totalLen: Int,
                    private val offset: Int): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val groupStateAndRefs = serviceHub.groupExists(groupId)
            if (groupStateAndRefs.isEmpty())
                throw DeepchainFlowException("Not in this group.")
            val miner = groupStateAndRefs.first().state.data.miner

            subFlow(SendParamFlow.SenderFlow(
                    param, StateType.PARAM, groupId, totalLen, offset, listOf(miner)
            ))
        }
    }
}