package com.deepchain.schemas

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object GroupSchema

/**
 * Schema to define database table to capture data in a group state.
 */
@CordaSerializable
object GroupSchemaV1 : MappedSchema(schemaFamily = GroupSchema.javaClass, version = 1,
        mappedTypes = listOf(PersistentGroupState::class.java)) {
    @Entity
    @Table(name = "group_states")
    class PersistentGroupState(

            @Column(name = "owner")
            var owner: Party? = null,

            @Column(name = "miner")
            var miner: Party? = null,

            @Column(name = "official")
            var official: Party? = null,

            @Column(name = "group_members")
            var groupMembers: String = "",

            @Column(name = "linear_id")
            var linearId: String = "",

            @Column(name = "max_num")
            var maxNum: Int = 1,

            @Column(name = "descroption")
            var descroption: String = "",

            @Column(name = "until_time")
            var untilTime: Instant? = null

    ) : PersistentState()
}