package com.deepchain.api

import net.corda.core.identity.CordaX500Name
import java.time.Instant

class CreateGroupData(
        val miner: String,
        val official: String,
        val maxNum: Int,
        val description: String = "",
        val untilTime: Instant
)

class JoinGroupData(val groupId: String, val ownerParty: CordaX500Name)