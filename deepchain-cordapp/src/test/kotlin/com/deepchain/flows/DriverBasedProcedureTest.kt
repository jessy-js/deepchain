package com.deepchain.flows

import com.deepchain.contracts.GroupContract
import com.deepchain.flows.group.ApplyGroupFlow
import com.deepchain.flows.group.CreateGroupFlow
import com.deepchain.utils.MinerUtils
import com.deepchain.utils.OfficialUtils
import com.deepchain.utils.PartyUtils
import com.deepchain.utils.VaultQueryUtils
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import org.junit.Test
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DriverBasedProcedureTest {

    @Test
    fun `procedure test`() {
        driver(DriverParameters(isDebug = true, startNodesInProcess = true,
                extraCordappPackagesToScan = CONTRACT_PACKAGES)) {
            // This starts two nodes simultaneously with startNode, which returns a future that completes when the node
            // has completed startup. Then these are all resolved with getOrThrow which returns the NodeHandle list.
            val (partyAHandle, partyBHandle, partyCHandle, partyDHandle) = listOf(
                    startNode(providedName = PARTYA_NAME),
                    startNode(providedName = PARTYB_NAME),
                    startNode(providedName = PARTYC_NAME),
                    startNode(providedName = PARTYD_NAME)
            ).map { it.getOrThrow() }
            val officialHandle = startNode(providedName = OFFICIAL_NAME).getOrThrow()
            val minerHandle = startNode(providedName = MINER_NAME).getOrThrow()

            val partyA = partyAHandle.nodeInfo.singleIdentity()
            val partyB = partyBHandle.nodeInfo.singleIdentity()
            val partyC = partyCHandle.nodeInfo.singleIdentity()
            val partyD = partyDHandle.nodeInfo.singleIdentity()
            val official = officialHandle.nodeInfo.singleIdentity()
            val miner = minerHandle.nodeInfo.singleIdentity()

            val filePathA = "C:\\00study01\\009 development\\000-deepchain-kotlin-master\\data\\worker 6\\worker 1.txt"
            val filePathB = "C:\\00study01\\009 development\\000-deepchain-kotlin-master\\data\\worker 6\\worker 2.txt"
            val filePathC = "C:\\00study01\\009 development\\000-deepchain-kotlin-master\\data\\worker 6\\worker 3.txt"
            val filePathD = "C:\\00study01\\009 development\\000-deepchain-kotlin-master\\data\\worker 6\\worker 4.txt"
            val outPath = "C:\\00study01\\009 development\\000-deepchain-kotlin-master\\output\\worker 1.txt"

            println("[DEEPCHAIN] Node initiate successfully!")

            // 上传之后有一定的延时
            // 所以在查询或者调用下一个 flow 之前要 sleep 一下
            val timeDelay: Long = 3
            // party A create group
            val flowFuture1 = partyAHandle.rpc.startFlow(CreateGroupFlow::OwnerFlow,
                    miner, official, 5, "AAA",
                    Instant.now().plusSeconds(65535)).returnValue
            val result = try {
                flowFuture1.getOrThrow()
            } catch (e: Exception) {
                println(e)
                return@driver
            }

            val groupId = (result.coreTransaction.outputStates.first() as GroupContract.State).linearId
            val message = "[DEEPCHAIN] Party A: Added a new group report for linear id $groupId " +
                    "in transaction ${result.tx.id} committed to ledger."
            println(message)

            val groupState = VaultQueryUtils.queryGroups(partyAHandle.rpc).first()
            assertEquals(groupState.state.data.official, officialHandle.nodeInfo.singleIdentity())
            assertEquals(groupState.state.data.miner, minerHandle.nodeInfo.singleIdentity())
            assertEquals(groupState.state.data.owner, partyAHandle.nodeInfo.singleIdentity())

            // party B join group
            val flowFuture2 = partyBHandle.rpc.startFlow(ApplyGroupFlow::Initiator, partyA, groupId).returnValue
            try {
                flowFuture2.getOrThrow()
            } catch (e: Exception) {
                println(e)
                return@driver
            }
            TimeUnit.SECONDS.sleep(timeDelay)
            println("[DEEPCHAIN] Party B: Join group successfully report for state id $groupId.")

            // party C join group
            val flowFuture3 = partyCHandle.rpc.startFlow(ApplyGroupFlow::Initiator, partyA, groupId).returnValue
            try {
                flowFuture3.getOrThrow()
            } catch (e: Exception) {
                println(e)
                return@driver
            }
            TimeUnit.SECONDS.sleep(timeDelay)
            println("[DEEPCHAIN] Party C: Join group successfully report for state id $groupId.")

            // party D join group
            val flowFuture4 = partyDHandle.rpc.startFlow(ApplyGroupFlow::Initiator, partyA, groupId).returnValue
            try {
                flowFuture4.getOrThrow()
            } catch (e: Exception) {
                println(e)
                return@driver
            }
            TimeUnit.SECONDS.sleep(timeDelay)
            println("[DEEPCHAIN] Party D: Join group successfully report for state id $groupId.")

            val groupStateB = VaultQueryUtils.queryGroups(partyBHandle.rpc).first()
            assertTrue(partyB in groupStateB.state.data.groupMembers)
            val groupStateC = VaultQueryUtils.queryGroups(partyCHandle.rpc).first()
            assertTrue(partyC in groupStateC.state.data.groupMembers)
            val groupStateD = VaultQueryUtils.queryGroups(partyDHandle.rpc).first()
            assertTrue(partyD in groupStateD.state.data.groupMembers)

            // official init group
            OfficialUtils.initiatePaillierApi(groupId.toString(), officialHandle.rpc)
            val unconsumedAlphaAndGStates = VaultQueryUtils.queryAlphaAndG(
                    officialHandle.rpc, groupId.toString())
            assertTrue(unconsumedAlphaAndGStates.isNotEmpty())

            // party A encrypt and upload
            PartyUtils.encryptAndUpload(partyAHandle.rpc, groupId.toString(), filePathA)
            val unconsumedParamStatesA = VaultQueryUtils.queryParams(
                    partyAHandle.rpc, groupId.toString(), "param")
            assertTrue(unconsumedParamStatesA.isNotEmpty())

            // party B encrypt and upload
            PartyUtils.encryptAndUpload(partyBHandle.rpc, groupId.toString(), filePathB)
            val unconsumedParamStatesB = VaultQueryUtils.queryParams(
                    partyBHandle.rpc, groupId.toString(), "param")
            assertTrue(unconsumedParamStatesB.isNotEmpty())

            // party C encrypt and upload
            PartyUtils.encryptAndUpload(partyCHandle.rpc, groupId.toString(), filePathC)
            val unconsumedParamStatesC = VaultQueryUtils.queryParams(
                    partyCHandle.rpc, groupId.toString(), "param")
            assertTrue(unconsumedParamStatesC.isNotEmpty())

            // party D encrypt and upload
            PartyUtils.encryptAndUpload(partyDHandle.rpc, groupId.toString(), filePathD)
            val unconsumedParamStatesD = VaultQueryUtils.queryParams(
                    partyDHandle.rpc, groupId.toString(), "param")
            assertTrue(unconsumedParamStatesD.isNotEmpty())

            // miner update
            MinerUtils.minerUpdate(minerHandle.rpc, groupId.toString())

            val unconsumedMinerStates = VaultQueryUtils.queryParams(
                    minerHandle.rpc, groupId.toString(), "param")
            val consumedMinerStates = VaultQueryUtils.queryParams(
                    minerHandle.rpc, groupId.toString(), "param", isConsumed = true)
            assertEquals(unconsumedMinerStates.size, unconsumedParamStatesA.size)
            assertEquals(unconsumedMinerStates.size, unconsumedParamStatesB.size)
            assertEquals(unconsumedMinerStates.size, unconsumedParamStatesC.size)
            assertEquals(unconsumedMinerStates.size, unconsumedParamStatesD.size)
            assertTrue(consumedMinerStates.isNotEmpty())

            // party A upload share
            PartyUtils.downloadAndUploadShare(partyAHandle.rpc, groupId.toString())

            val unconsumedShareStatesA = VaultQueryUtils.queryParams(
                    partyAHandle.rpc, groupId.toString(), "decryptshare")
            val consumedShareStatesA = VaultQueryUtils.queryParams(
                    partyAHandle.rpc, groupId.toString(), "decryptshare", isConsumed = true)
            assertTrue(unconsumedShareStatesA.isNotEmpty())
            assertTrue(consumedShareStatesA.isEmpty())

            // party B upload share
            PartyUtils.downloadAndUploadShare(partyBHandle.rpc, groupId.toString())

            val unconsumedShareStatesB = VaultQueryUtils.queryParams(
                    partyBHandle.rpc, groupId.toString(), "decryptshare")
            val consumedShareStatesB = VaultQueryUtils.queryParams(
                    partyBHandle.rpc, groupId.toString(), "decryptshare", isConsumed = true)
            assertTrue(unconsumedShareStatesB.isNotEmpty())
            assertTrue(consumedShareStatesB.isEmpty())

            // party C upload share
            PartyUtils.downloadAndUploadShare(partyCHandle.rpc, groupId.toString())

            val unconsumedShareStatesC = VaultQueryUtils.queryParams(
                    partyCHandle.rpc, groupId.toString(), "decryptshare")
            val consumedShareStatesC = VaultQueryUtils.queryParams(
                    partyCHandle.rpc, groupId.toString(), "decryptshare", isConsumed = true)
            assertTrue(unconsumedShareStatesC.isNotEmpty())
            assertTrue(consumedShareStatesC.isEmpty())

            // party D upload share
            PartyUtils.downloadAndUploadShare(partyDHandle.rpc, groupId.toString())

            val unconsumedShareStatesD = VaultQueryUtils.queryParams(
                    partyDHandle.rpc, groupId.toString(), "decryptshare")
            val consumedShareStatesD = VaultQueryUtils.queryParams(
                    partyDHandle.rpc, groupId.toString(), "decryptshare", isConsumed = true)
            assertTrue(unconsumedShareStatesD.isNotEmpty())
            assertTrue(consumedShareStatesD.isEmpty())

            // party A decrypt
            TimeUnit.SECONDS.sleep(timeDelay)
            PartyUtils.downloadShareAndDecrypt(partyBHandle.rpc, groupId.toString(), outPath)
        }
    }
}
