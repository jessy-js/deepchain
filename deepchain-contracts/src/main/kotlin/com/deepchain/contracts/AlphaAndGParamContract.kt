package com.deepchain.contracts

import com.deepchain.data.bigIntegerList2BigDecimalList
import com.deepchain.schemas.AlphaAndGParamSchemaV1
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.TransactionBuilder
import java.math.BigDecimal
import java.math.BigInteger
import java.security.PublicKey

class AlphaAndGParamContract: Contract {
    companion object {
        const val ALPHA_AND_G_PARAM_CONTRACT_ID: ContractClassName =
                "com.deepchain.contracts.AlphaAndGParamContract"

        @JvmStatic
        fun generateCreate(tx: TransactionBuilder, owner: Party, alphaList: List<BigInteger>,
                           gList: List<BigInteger>, groupMembers: List<Party>,
                           groupId: UniqueIdentifier, notary: Party) : PublicKey {
            check(tx.inputStates().isEmpty())
            check(tx.outputStates().isEmpty())

            tx.addOutputState(
                    TransactionState(
                            State(
                                    UniqueIdentifier(), owner,
                                    bigIntegerList2BigDecimalList(alphaList),
                                    bigIntegerList2BigDecimalList(gList),
                                    groupMembers, groupId
                            ), ALPHA_AND_G_PARAM_CONTRACT_ID, notary
                    )
            )
            tx.addCommand(Commands.Create(), listOf(owner.owningKey))

            return owner.owningKey
        }

    }

    override fun verify(tx: LedgerTransaction) {
        val groups = tx.groupStates { it: State -> it.linearId }

        val command = tx.commands.requireSingleCommand<Commands>()
        for ((inputs, outputs, _) in groups) {
            when (command.value) {
                is Commands.Create -> verifyCreateCommand(tx, inputs, outputs)
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
    }

    data class State(override val linearId: UniqueIdentifier,
                     val owner: Party,
                     val alphaList: List<BigDecimal>,
                     val gList: List<BigDecimal>,
                     val groupMembers: List<Party>,
                     val groupId: UniqueIdentifier): LinearState, QueryableState {

        override val participants: List<AbstractParty> get() = groupMembers + owner

        override fun generateMappedObject(schema: MappedSchema): PersistentState {
            return when (schema) {
                is AlphaAndGParamSchemaV1 -> AlphaAndGParamSchemaV1.PersistentAlphaAndGParamState(
                        linearId = this.linearId.toString(),
                        owner = this.owner,
                        groupMembers = this.groupMembers.toString(),
                        groupId = groupId.toString(),
                        alphaList = alphaList.toString(),
                        gList = gList.toString()
                )
                else -> throw IllegalArgumentException("Unrecognized schema $schema")
            }
        }

        override fun supportedSchemas(): Iterable<MappedSchema> = listOf(AlphaAndGParamSchemaV1)
    }

    interface Commands: CommandData {
        class Create: Commands
    }
}