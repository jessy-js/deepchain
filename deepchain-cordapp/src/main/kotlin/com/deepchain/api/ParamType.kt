package com.deepchain.api

import java.math.BigInteger

class SendParamData(
        val param: List<List<BigInteger>>,
        val groupId: String,
        val totalLen: Int,
        val offset: Int
)

class MinerUpdateData(
        val groupId: String
)
