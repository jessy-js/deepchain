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

object CreateDecryptShareFlow {
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
            val me = serviceHub.myInfo.legalIdentities.first()
            val partyList = groupStateAndRefs.first().state.data.groupMembers +
                    groupStateAndRefs.first().state.data.owner - me
            // todo 这里只有加上了 miner 才能在 test 里面看到 state
            // 但是在实际运行的时候是是正常的
            // val partyList = listOf(groupStateAndRefs.first().state.data.miner)

            subFlow(SendParamFlow.SenderFlow(
                    param, StateType.DECRYPTSHARE, groupId, totalLen, offset, partyList
            ))
        }
    }
}