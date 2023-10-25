package com.deepchain.contracts

import com.deepchain.data.StateType
import com.deepchain.data.bigIntegerListList2BigDecimalListList
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey
import com.deepchain.schemas.ParamSchemaV1
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Contract to define param state, commands and verifications.
 */
class ParamContract: Contract {
    companion object {
        const val PARAM_CONTRACT_ID: ContractClassName = "com.deepchain.contracts.ParamContract"

        @JvmStatic
        fun generateCreateParam(tx: TransactionBuilder, owner: Party, official: Party, receiverList: List<Party>,
                                stateType: StateType, param: List<List<BigInteger>>, groupId: UniqueIdentifier,
                                totalLen: Int, offset: Int, notary: Party): PublicKey {
            check(tx.inputStates().isEmpty())
            check(tx.outputStates().isEmpty())

            tx.addOutputState(
                    TransactionState(
                            State(
                                    UniqueIdentifier(), owner, official, receiverList, stateType,
                                    bigIntegerListList2BigDecimalListList(param),
                                    groupId, totalLen, offset
                            ), PARAM_CONTRACT_ID, notary))
            val keyList = mutableListOf<PublicKey>()
            for (party in receiverList) {
                keyList.add(party.owningKey)
            }
            keyList.add(owner.owningKey)
            if (stateType == StateType.PARAM) {
                tx.addCommand(Commands.CreateParam(), keyList)
            }
            else {
                tx.addCommand(Commands.CreateShare(), keyList)
            }

            return owner.owningKey
        }

        @JvmStatic
        fun generateMinerUpdate(tx: TransactionBuilder, owner: Party, official: Party, receiverList: List<Party>,
                                stateType: StateType, param: List<List<BigInteger>>, groupId: UniqueIdentifier,
                                totalLen: Int, offset: Int, notary: Party,
                                stateAndRefList: List<StateAndRef<State>>) {
            check(tx.inputStates().isEmpty())
            check(tx.outputStates().isEmpty())

            for (stateAndRef in stateAndRefList) {
                tx.addInputState(stateAndRef)
            }
            tx.addOutputState(
                    TransactionState(
                            State(
                                    UniqueIdentifier(), owner, official, receiverList, stateType,
                                    bigIntegerListList2BigDecimalListList(param),
                                    groupId, totalLen, offset
                            ), PARAM_CONTRACT_ID, notary))
            val keyList = mutableListOf<PublicKey>()
            for (party in receiverList) {
                keyList.add(party.owningKey)
            }
            keyList.add(owner.owningKey)
            tx.addCommand(Commands.MinerUpdate(), keyList)
        }
    }

    override fun verify(tx: LedgerTransaction) {
        val groups = tx.groupStates { it: State -> it.groupId }

        val command = tx.commands.requireSingleCommand<Commands>()
        for ((inputs, outputs, _) in groups) {
            when (command.value) {
                is Commands.CreateParam -> verifyCreateParamCommand(tx, inputs, outputs)
                is Commands.MinerUpdate -> verifyMinerUpdateCommand(tx, inputs, outputs)
                is Commands.CreateShare -> verifyCreateShareCommand(tx, inputs, outputs)
            }
        }
    }

    private fun verifyCreateParamCommand(tx: LedgerTransaction, inputs: List<State>, outputs: List<State>) {
        val createCommand = tx.commands.requireSingleCommand<Commands.CreateParam>()
        requireThat {
            "there are no input states" using (inputs.count() == 0)
            "there is at least one output state" using (outputs.count() >= 1)
            "there is only one receiver" using (outputs.first().receiverList.size == 1)
            "owner and miner are signers on the command" using
                    (createCommand.signers.containsAll(
                            listOf(outputs.first().owner.owningKey, outputs.first().receiverList[0].owningKey)
                    ))
        }

        for (outputState in outputs) {
            verifyParamDetails(outputState)
        }
    }

    private fun verifyMinerUpdateCommand(tx: LedgerTransaction, inputs: List<State>, outputs: List<State>) {
        requireThat {
            "there is at least one input state" using (inputs.count() >= 1)
            "there is at least one output state" using (outputs.count() >= 1)
        }

        for (outputState in outputs) {
            verifyParamDetails(outputState)
        }
    }

    private fun verifyCreateShareCommand(tx: LedgerTransaction, inputs: List<State>, outputs: List<State>) {
        requireThat {
            "there are no input states" using (inputs.count() == 0)
            "there is a single output state" using (outputs.count() == 1)
            "there is at least one receiver" using (outputs.first().receiverList.isNotEmpty())
        }

        for (outputState in outputs) {
            verifyDecryptShareDetails(outputState)
        }
    }

    private fun verifyParamDetails(paramState: State) {
        requireThat {
            "total length is at least one" using (paramState.totalLen > 0)
            "offset is at least zero" using (paramState.offset >= 0)
            "param should not be null" using (paramState.param.isNotEmpty())
            "param should have exactly nine elements" using (paramState.param.first().size == 9)
        }
    }

    private fun verifyDecryptShareDetails(paramState: State) {
        requireThat {
            "total length is at least one" using (paramState.totalLen > 0)
            "offset is at least zero" using (paramState.offset >= 0)
            "param should not be null" using (paramState.param.isNotEmpty())
            "param should have exactly eight elements" using (paramState.param.first().size == 8)
        }
    }

    data class State(override val linearId: UniqueIdentifier,
                     val owner: Party,
                     val official: Party,
                     val receiverList: List<Party>,
                     val stateType: StateType,
                     val param: List<List<BigDecimal>>,
                     val groupId: UniqueIdentifier,
                     val totalLen: Int,
                     val offset: Int): LinearState, QueryableState {

        override val participants: List<AbstractParty> get() =
            if (receiverList.isNotEmpty()) receiverList + owner + official
            else listOf(owner, official)

        override fun generateMappedObject(schema: MappedSchema): PersistentState {
            return when (schema) {
                is ParamSchemaV1 -> ParamSchemaV1.PersistentParamState(
                        owner = this.owner,
                        official = this.official,
                        receiverList = this.receiverList.toString(),
                        stateType = this.stateType.toString(),
                        param = this.param.toString(),
                        linearId = this.linearId.toString(),
                        groupId = this.groupId.toString(),
                        totalLen = this.totalLen,
                        offset_num = this.offset
                )
                else -> throw IllegalArgumentException("Unrecognized schema $schema")
            }
        }

        override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ParamSchemaV1)
    }

    interface Commands: CommandData {
        class CreateParam: Commands
        class MinerUpdate: Commands
        class CreateShare: Commands
    }
}