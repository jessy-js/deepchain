package com.deepchain.schemas

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object PublicAndPrivateParamsSchema

/**
 * Schema to define database table to capture data in a group state.
 */
@CordaSerializable
object PublicAndPrivateParamsSchemaV1: MappedSchema(schemaFamily = PublicAndPrivateParamsSchema.javaClass, version = 1,
        mappedTypes = listOf(PersistentPublicAndPrivateParamsState::class.java)) {
    @Entity
    @Table(name = "alpha_and_g_param_states")
    class PersistentPublicAndPrivateParamsState(

            @Column(name = "owner")
            var owner: Party? = null,

            @Column(name = "param_list", columnDefinition = "LONGTEXT")
            var paramList: String = "",

            @Column(name = "private_key", columnDefinition = "LONGTEXT")
            var paivateKey: String = "",

            @Column(name = "receiver")
            var receiver: String = "",

            @Column(name = "linear_id")
            var linearId: String = "",

            @Column(name = "group_id")
            var groupId: String = ""

    ) : PersistentState()
}