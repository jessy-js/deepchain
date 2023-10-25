package com.deepchain.flows

import com.deepchain.contracts.GroupContract
import com.deepchain.contracts.ParamContract
import com.deepchain.flows.group.ApplyGroupFlow
import com.deepchain.flows.group.ApproveGroupFlow
import com.deepchain.flows.group.CreateGroupFlow
import com.deepchain.flows.group.UpdateGroupFlow
import com.deepchain.flows.param.CreateDecryptShareFlow
import com.deepchain.flows.param.PartyUploadFlow
import com.deepchain.flows.param.SendParamFlow
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigInteger
import java.time.Instant
import kotlin.test.assertEquals

class ParamFlowTest {
    private lateinit var mockNet: MockNetwork
    private lateinit var partyA: StartedMockNode
    private lateinit var partyB: StartedMockNode
    private lateinit var partyC: StartedMockNode
    private lateinit var partyD: StartedMockNode
    private lateinit var partyE: StartedMockNode
    private lateinit var miner: StartedMockNode
    private lateinit var official: StartedMockNode
    private lateinit var notary: Party
    private lateinit var testParam: List<List<BigInteger>>
    private lateinit var testShare: List<List<BigInteger>>

    @Before
    fun setup() {
        mockNet = MockNetwork(cordappPackages = CONTRACT_PACKAGES)
        partyA = mockNet.createNode(PARTYA_NAME)
        partyB = mockNet.createNode(PARTYB_NAME)
        partyC = mockNet.createNode(PARTYC_NAME)
        partyD = mockNet.createNode(PARTYD_NAME)
        partyE = mockNet.createNode(PARTYE_NAME)
        miner = mockNet.createNode(MINER_NAME)
        official = mockNet.createNode(OFFICIAL_NAME)
        notary = mockNet.defaultNotaryIdentity

        miner.registerInitiatedFlow(CreateGroupFlow.MinerFlow::class.java)
        official.registerInitiatedFlow(CreateGroupFlow.MinerFlow::class.java)
        partyA.registerInitiatedFlow(ApproveGroupFlow::class.java)
        partyB.registerInitiatedFlow(UpdateGroupFlow.ApplicantFlow::class.java)
        partyC.registerInitiatedFlow(UpdateGroupFlow.ApplicantFlow::class.java)
        partyD.registerInitiatedFlow(UpdateGroupFlow.ApplicantFlow::class.java)
        miner.registerInitiatedFlow(SendParamFlow.ReceiverFlow::class.java)
        partyA.registerInitiatedFlow(SendParamFlow.ReceiverFlow::class.java)
        partyB.registerInitiatedFlow(SendParamFlow.ReceiverFlow::class.java)
        partyC.registerInitiatedFlow(SendParamFlow.ReceiverFlow::class.java)
        partyD.registerInitiatedFlow(SendParamFlow.ReceiverFlow::class.java)

        testParam = mutableListOf(listOf(BigInteger.ONE, BigInteger.TEN, BigInteger.ONE,
                BigInteger.TEN, BigInteger.ONE, BigInteger.TEN,
                BigInteger.TEN, BigInteger.ONE, BigInteger.TEN))
        testShare = mutableListOf(listOf(BigInteger.TEN))
    }

    @After
    fun cleanup() {
        mockNet.stopNodes()
    }

    @Test
    fun `all param operations`() {
        // party A create group
        val createGroupResultFuture = partyA.startFlow(CreateGroupFlow.OwnerFlow(
                miner.info.singleIdentity(), official.info.singleIdentity(),
                4, "Just do blockchain",
                Instant.now().plusSeconds(65535)))
        mockNet.runNetwork()

        val groupResult = createGroupResultFuture.getOrThrow()

        val groupId = (groupResult.coreTransaction.outputStates.first() as GroupContract.State).linearId

        // party B join group
        partyB.startFlow(ApplyGroupFlow.Initiator(
                partyA.info.singleIdentity(), groupId
        ))
        mockNet.runNetwork()

        // party C join group
        partyC.startFlow(ApplyGroupFlow.Initiator(
                partyA.info.singleIdentity(), groupId
        ))
        mockNet.runNetwork()

        // party D join group
        partyD.startFlow(ApplyGroupFlow.Initiator(
                partyA.info.singleIdentity(), groupId
        ))
        mockNet.runNetwork()

        for (i in 0 until 5) {
            partyA.startFlow(PartyUploadFlow.Initiator(
                    testParam, groupId, 5, i
            ))
            mockNet.runNetwork()
        }
        for (i in 0 until 5) {
            partyB.startFlow(PartyUploadFlow.Initiator(
                    testParam, groupId, 5, i
            ))
            mockNet.runNetwork()
        }
        for (i in 0 until 5) {
            partyC.startFlow(PartyUploadFlow.Initiator(
                    testParam, groupId, 5, i
            ))
            mockNet.runNetwork()
        }
        for (i in 0 until 5) {
            partyD.startFlow(PartyUploadFlow.Initiator(
                    testParam, groupId, 5, i
            ))
            mockNet.runNetwork()
        }

//        miner.startFlow(MinerUpdateFlow.Initiator(groupId))
//        mockNet.runNetwork()

        val unconsumedStates = miner.services.vaultService.queryBy(ParamContract.State::class.java).states
        val consumedStates = miner.services.vaultService.queryBy(
                ParamContract.State::class.java,
                QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED)).states
        println("output states len: " + unconsumedStates.size)
        println("input states len: " + consumedStates.size)

        assertEquals(unconsumedStates.size, 20)
        assertEquals(consumedStates.size, 0)

        for (i in 0 until 5) {
            partyA.startFlow(CreateDecryptShareFlow.Initiator(
                    testShare, groupId, 5, i
            ))
            mockNet.runNetwork()
        }
        for (i in 0 until 5) {
            partyB.startFlow(CreateDecryptShareFlow.Initiator(
                    testShare, groupId, 5, i
            ))
            mockNet.runNetwork()
        }
        for (i in 0 until 5) {
            partyC.startFlow(CreateDecryptShareFlow.Initiator(
                    testShare, groupId, 5, i
            ))
            mockNet.runNetwork()
        }
        for (i in 0 until 5) {
            partyD.startFlow(CreateDecryptShareFlow.Initiator(
                    testShare, groupId, 5, i
            ))
            mockNet.runNetwork()
        }

        val decrtptShareStates = partyE.services.vaultService.queryBy(ParamContract.State::class.java).states
        assertEquals(decrtptShareStates.size, 40)
    }
}