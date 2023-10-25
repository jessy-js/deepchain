package com.deepchain.flows

import com.deepchain.contracts.GroupContract
import com.deepchain.flows.group.ApplyGroupFlow
import com.deepchain.flows.group.ApproveGroupFlow
import com.deepchain.flows.group.CreateGroupFlow
import com.deepchain.flows.group.UpdateGroupFlow
import net.corda.core.contracts.TransactionVerificationException
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
import kotlin.test.*

class GroupFlowTest {
    private lateinit var mockNet: MockNetwork
    private lateinit var partyA: StartedMockNode
    private lateinit var partyB: StartedMockNode
    private lateinit var partyC: StartedMockNode
    private lateinit var partyD: StartedMockNode
    private lateinit var official: StartedMockNode
    private lateinit var miner: StartedMockNode
    private lateinit var notary: Party
    private lateinit var testParam: List<List<BigInteger>>

    @Before
    fun setup() {
        mockNet = MockNetwork(cordappPackages = CONTRACT_PACKAGES)
        partyA = mockNet.createNode(PARTYA_NAME)
        partyB = mockNet.createNode(PARTYB_NAME)
        partyC = mockNet.createNode(PARTYC_NAME)
        partyD = mockNet.createNode(PARTYD_NAME)
        official = mockNet.createNode(OFFICIAL_NAME)
        miner = mockNet.createNode(MINER_NAME)
        notary = mockNet.defaultNotaryIdentity

        miner.registerInitiatedFlow(CreateGroupFlow.MinerFlow::class.java)
        official.registerInitiatedFlow(CreateGroupFlow.MinerFlow::class.java)
        partyA.registerInitiatedFlow(ApproveGroupFlow::class.java)
        partyB.registerInitiatedFlow(UpdateGroupFlow.ApplicantFlow::class.java)
        partyC.registerInitiatedFlow(UpdateGroupFlow.ApplicantFlow::class.java)
        partyD.registerInitiatedFlow(UpdateGroupFlow.ApplicantFlow::class.java)

        testParam = mutableListOf(listOf(BigInteger.ONE, BigInteger.TEN),
                listOf(BigInteger.ONE, BigInteger.TEN))
    }

    @After
    fun cleanup() {
        mockNet.stopNodes()
    }

    @Test
    fun `all group operations`() {
        // party A create group
        val createGroupResultFuture = partyA.startFlow(CreateGroupFlow.OwnerFlow(
                miner.info.singleIdentity(), official.info.singleIdentity(),
                4, "JOJO, I won't be a human anymore!",
                Instant.now().plusSeconds(65535)))
        mockNet.runNetwork()

        val groupResult = createGroupResultFuture.getOrThrow()

        assertNotNull(partyA.services.validatedTransactions.getTransaction(groupResult.id))
        assertNotNull(miner.services.validatedTransactions.getTransaction(groupResult.id))

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

        val groupFinalState = partyA.services.vaultService.queryBy(GroupContract.State::class.java).states
        val groupConsumedStates = partyA.services.vaultService.queryBy(
                GroupContract.State::class.java,
                QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED)
        ).states
        val groupFinalStateData = groupFinalState.first().state.data
        assertEquals(groupFinalState.size, 1)
        assertEquals(groupConsumedStates.size, 3)
        assertEquals(groupFinalStateData.owner, partyA.info.legalIdentities.first())
        assertEquals(groupFinalStateData.miner, miner.info.legalIdentities.first())
        assertTrue(partyB.info.legalIdentities.first() in groupFinalStateData.groupMembers)
        assertTrue(partyC.info.legalIdentities.first() in groupFinalStateData.groupMembers)
        assertTrue(partyD.info.legalIdentities.first() in groupFinalStateData.groupMembers)
    }

    @Test
    fun `invalid max num`() {
        val createGroupResultFuture = partyA.startFlow(CreateGroupFlow.OwnerFlow(
                miner.info.singleIdentity(), official.info.singleIdentity(),
                -1, "Wow, awesome, blockchain", Instant.now().plusSeconds(65535)))
        mockNet.runNetwork()

        assertFailsWith<TransactionVerificationException> {
            createGroupResultFuture.getOrThrow()
        }
    }

    // 单独运行就能通过，所有一起运行就会出现错误
    // java.lang.IllegalStateException:
    // Was not expecting to find existing database
    // transaction on current strand when setting database
    @Test
    fun `too many participants`() {
        val createGroupResultFuture = partyA.startFlow(CreateGroupFlow.OwnerFlow(
                miner.info.singleIdentity(), official.info.singleIdentity(),
                2, "Bbbbbbbbbblockchain", Instant.now().plusSeconds(65535)))
        mockNet.runNetwork()

        val groupResult = createGroupResultFuture.getOrThrow()
        val groupId = (groupResult.coreTransaction.outputStates.first() as GroupContract.State).linearId

        // party B join group
        partyB.startFlow(ApplyGroupFlow.Initiator(
                partyA.info.singleIdentity(), groupId
        ))
        mockNet.runNetwork()

        // party C join group
        // this won't raise TransactionVerificationException
        // because exception occurs in the subflow, but not this flow
        partyC.startFlow(ApplyGroupFlow.Initiator(
                partyA.info.singleIdentity(), groupId
        ))
        mockNet.runNetwork()

        val groupFinalState = partyA.services.vaultService.queryBy(GroupContract.State::class.java).states
        val groupFinalStateData = groupFinalState.first().state.data
        assertTrue(partyB.info.legalIdentities.first() in groupFinalStateData.groupMembers)
        assertFalse(partyD.info.legalIdentities.first() in groupFinalStateData.groupMembers)
    }
}