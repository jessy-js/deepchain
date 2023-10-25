package com.deepchain.plugin

import com.paillier.entity.Ciphertext
import com.paillier.entity.ProofPKEntity
import net.corda.core.serialization.SerializationWhitelist
import java.math.BigInteger
import java.util.*

class MySerializationWhitelist: SerializationWhitelist {
    override val whitelist = listOf(BigInteger::class.java, LinkedList::class.java,
            Ciphertext::class.java, ProofPKEntity::class.java)
}