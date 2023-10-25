package com.deepchain.data

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class StateType {
    PARAM,
    DECRYPTSHARE,
}
