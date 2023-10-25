package com.deepchain.data

import java.math.BigDecimal
import java.math.BigInteger

fun bigIntegerList2BigDecimalList(bigIntegerList: List<BigInteger>): List<BigDecimal> {
    val bigDecimalList = mutableListOf<BigDecimal>()
    for (bigInteger in bigIntegerList) {
        bigDecimalList.add(BigDecimal(bigInteger))
    }

    return bigDecimalList
}

fun bigDecimalList2BigIntegerList(bigDecimalList: List<BigDecimal>): List<BigInteger> {
    val bigIntegerList = mutableListOf<BigInteger>()
    for (bigDecimal in bigDecimalList) {
        bigIntegerList.add(bigDecimal.toBigInteger())
    }

    return bigIntegerList
}

fun bigIntegerListList2BigDecimalListList(bigIntegerListList: List<List<BigInteger>>): List<List<BigDecimal>> {
    val bigDecimalListList = mutableListOf<List<BigDecimal>>()
    for (bigIntegerList in bigIntegerListList) {
        bigDecimalListList.add(bigIntegerList2BigDecimalList(bigIntegerList))
    }

    return bigDecimalListList
}

fun bigDecimalListList2BigIntegerListList(bigDecimalListList: List<List<BigDecimal>>): List<List<BigInteger>> {
    val bigIntegerListList = mutableListOf<List<BigInteger>>()
    for (bigDecimalList in bigDecimalListList) {
        bigIntegerListList.add(bigDecimalList2BigIntegerList(bigDecimalList))
    }

    return bigIntegerListList
}
