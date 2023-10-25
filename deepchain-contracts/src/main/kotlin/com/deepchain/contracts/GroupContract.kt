package com.deepchain.contracts

import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey
import com.deepchain.schemas.GroupSchemaV1
import net.corda.core.contracts.*

/**
 * Contract to define group state, commands and verifications.
 */
class GroupContract: Contract {
    companion object {
        const val GROUP_CONTRACT_ID: ContractClassName = "com.deepchain.contracts.GroupContract"

        @JvmStatic
        fun generateCreate(tx: TransactionBuilder, owner: Party,
                           miner: Party, official: Party,
                           maxNum: Int, description: String,
                           untilTime: TimeWindow, notary: Party) : PublicKey {
            check(tx.inputStates().isEmpty())
            check(tx.outputStates().isEmpty())

            tx.addOutputState(
                    TransactionState(
                            State(
                                    UniqueIdentifier(), owner, miner, official,
                                    listOf(), maxNum, description, untilTime
                            ), GROUP_CONTRACT_ID, notary
                    )
            )
            tx.addCommand(Commands.Create(), listOf(owner.owningKey, miner.owningKey, official.owningKey))

            return owner.owningKey
        }

        @JvmStatic
        fun generateUpdate(tx: TransactionBuilder, stateAndRef: StateAndRef<State>,
                           newGroupMember: Party, notary: Party) {
            check(tx.inputStates().isEmpty())
            check(tx.outputStates().isEmpty())

            val updatedState = stateAndRef.state.data.copy(
                    groupMembers = stateAndRef.state.data.groupMembers + newGroupMember)

            tx.addOutputState(TransactionState(updatedState, GROUP_CONTRACT_ID, notary))
            tx.addInputState(stateAndRef)
            tx.setTimeWindow(stateAndRef.state.data.untilTime)  // 在规定时间之前可以加入

            val owner = stateAndRef.state.data.owner
            tx.addCommand(Commands.Update(), listOf(owner.owningKey, newGroupMember.owningKey))
        }
    }

    override fun verify(tx: LedgerTransaction) {
        val groups = tx.groupStates { it: State -> it.linearId }

        val command = tx.commands.requireSingleCommand<Commands>()
        for ((inputs, outputs, _) in groups) {
            when (command.value) {
                is Commands.Create -> verifyCreateCommand(tx, inputs, outputs)
                is Commands.Update -> verifyUpdateCommand(tx, inputs, outputs)
            }
        }
    }

    private fun verifyCreateCommand(tx: LedgerTransaction, inputs: List<State>, outputs: List<State>) {
        val createCommand = tx.commands.requireSingleCommand<Commands.Create>()
        requireThat {
            "there are no input states" using (inputs.count() == 0)
            "there is a single output state" using (outputs.count() == 1)
            "owner is signer on the command" using
                    (createCommand.signers.contains(outputs.first().owner.owningKey))
        }

        verifyMaxNum(outputs.first().maxNum, 0)
    }

    private fun verifyUpdateCommand(tx: LedgerTransaction, inputs: List<State>, outputs: List<State>) {
        tx.commands.requireSingleCommand<Commands.Update>()
        requireThat {
            "there is a single input state" using (inputs.count() == 1)
            "there is a single output state" using (outputs.count() == 1)
        }

        val inputMembersLen = inputs.first().groupMembers.size
        val outputMembersLen = outputs.first().groupMembers.size

        requireThat {
            "group members have been updated" using (inputMembersLen != outputMembersLen)
        }

        verifyMaxNum(outputs.first().maxNum, outputMembersLen)
    }

    private fun verifyMaxNum(maxNum: Int, curNum: Int) {
        requireThat {
            "max num is at least one" using (maxNum > 0)
        }
        requireThat {
            "current num is not greater than maxNum" using (curNum < maxNum)
        }
    }

    data class State(override val linearId: UniqueIdentifier,
                     val owner: Party,
                     val miner: Party,
                     val official: Party,
                     val groupMembers: List<Party>,
                     val maxNum: Int,
                     val description: String,
                     val untilTime: TimeWindow): LinearState, QueryableState {

        override val participants: List<AbstractParty> get() =
            if (groupMembers.isNotEmpty()) groupMembers + owner + miner + official
            else listOf(owner, miner, official)

        override fun generateMappedObject(schema: MappedSchema): PersistentState {
            return when (schema) {
                is GroupSchemaV1 -> GroupSchemaV1.PersistentGroupState(
                        linearId = this.linearId.toString(),
                        owner = this.owner,
                        miner = this.miner,
                        official = this.official,
                        groupMembers = this.groupMembers.toString(),
                        maxNum = this.maxNum,
                        descroption = this.description,
                        untilTime = this.untilTime.untilTime
                )
                else -> throw IllegalArgumentException("Unrecognized schema $schema")
            }
        }

        override fun supportedSchemas(): Iterable<MappedSchema> = listOf(GroupSchemaV1)
    }

    interface Commands: CommandData {
        class Create: Commands
        class Update: Commands
    }
}