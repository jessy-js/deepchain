package com.deepchain.flows

import net.corda.core.flows.FlowException

class DeepchainFlowException(message: String, cause: Throwable? = null): FlowException(message, cause)