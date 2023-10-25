package com.deepchain.schemas

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object AlphaAndGParamSchema

/**
 * Schema to define database table to capture data in a group state.
 */
@CordaSerializable
object AlphaAndGParamSchemaV1: MappedSchema(schemaFamily = AlphaAndGParamSchema.javaClass, version = 1,
        mappedTypes = listOf(PersistentAlphaAndGParamState::class.java)) {
    @Entity
    @Table(name = "alpha_and_g_param_states")
    class PersistentAlphaAndGParamState(

            @Column(name = "owner")
            var owner: Party? = null,

            @Column(name = "alpha_list", columnDefinition = "LONGTEXT")
            var alphaList: String = "",

            @Column(name = "g_list", columnDefinition = "LONGTEXT")
            var gList: String = "",

            @Column(name = "group_members")
            var groupMembers: String = "",

            @Column(name = "linear_id")
            var linearId: String = "",

            @Column(name = "group_id")
            var groupId: String = ""

    ) : PersistentState()
}