package com.deepchain.flows

import net.corda.core.identity.CordaX500Name

val CONTRACT_PACKAGES = listOf("com.deepchain.contracts", "com.deepchain.schemas")

val PARTYA_NAME = CordaX500Name(
        commonName = "Party A", organisation = "One Piece Inc", locality = "Tokyo", country = "JP")
val PARTYB_NAME = CordaX500Name(
        commonName = "Party B", organisation = "Marvel Inc", locality = "New York", country = "US")
val PARTYC_NAME = CordaX500Name(
        commonName = "Party C", organisation = "Shenjing Inc", locality = "Beijing", country = "CN")
val PARTYD_NAME = CordaX500Name(
        commonName = "Party D", organisation = "Paomian Inc", locality = "Guangzhou", country = "CN")
val PARTYE_NAME = CordaX500Name(
        commonName = "Party E", organisation = "Backup Inc", locality = "Wuhan", country = "CN")
val MINER_NAME = CordaX500Name(
        commonName = "Miner", organisation = "Mine & Dig Inc", locality = "London", country = "GB")
val OFFICIAL_NAME = CordaX500Name(
        commonName = "Official", organisation = "United Nation", locality = "New York", country = "US")