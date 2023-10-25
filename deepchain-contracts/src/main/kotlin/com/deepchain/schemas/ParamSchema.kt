package com.deepchain.schemas

import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object ParamSchema

/**
 * Schema to define database table to capture data in a param state.
 */
@CordaSerializable
object ParamSchemaV1: MappedSchema(schemaFamily = ParamSchema.javaClass, version = 1,
        mappedTypes = listOf(PersistentParamState::class.java)) {
    @Entity
    @Table(name = "param_states")
    class PersistentParamState(

            // basic info
            @Column(name = "owner")
            var owner: AbstractParty? = null,

            @Column(name = "official")
            var official: AbstractParty? = null,

            @Column(name = "receiverList")
            var receiverList: String = "",

            @Column(name = "state_type")
            var stateType: String = "",

            @Column(name = "param", columnDefinition = "LONGTEXT")
            var param: String = "",

            // control info
            @Column(name = "linear_id")
            var linearId: String = "",

            @Column(name = "group_id")
            var groupId: String = "",

            @Column(name = "total_len")
            var totalLen: Int = 1,

            @Column(name = "offset_num")
            var offset_num: Int = 0

    ): PersistentState()
}