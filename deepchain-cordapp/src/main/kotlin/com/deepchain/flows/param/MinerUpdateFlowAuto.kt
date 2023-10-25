package com.deepchain.flows.param

import co.paralleluniverse.fibers.Suspendable
import com.deepchain.contracts.ParamContract
import com.deepchain.data.StateType
import com.deepchain.flows.DeepchainFlowException
import com.deepchain.flows.groupExists
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import java.math.BigInteger

/**
 * Flow to send updated param to every party in the group.
 * This flow creates a param state that are visible to every party.
 */
object MinerUpdateFlowAuto {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val param: List<List<BigInteger>>,
                    private val groupId: UniqueIdentifier,
                    private val totalLen: Int,
                    private val offset: Int,
                    private val stateAndRefList: List<StateAndRef<ParamContract.State>>): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val groupStateAndRefs = serviceHub.groupExists(groupId)
            if (groupStateAndRefs.isEmpty())
                throw DeepchainFlowException("Not in this group.")
            val partyList = groupStateAndRefs.first().state.data.groupMembers +
                    groupStateAndRefs.first().state.data.owner

            subFlow(SendParamFlow.SenderFlow(
                    param, StateType.PARAM, groupId, totalLen,
                    offset, partyList, stateAndRefList
            ))
        }
    }
}