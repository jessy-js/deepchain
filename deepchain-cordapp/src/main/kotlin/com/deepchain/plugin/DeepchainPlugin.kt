package com.deepchain.plugin

import com.deepchain.api.GroupApi
import com.deepchain.api.InfoApi
import com.deepchain.api.OfficialApi
import com.deepchain.api.ParamApi
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function
import com.fasterxml.jackson.databind.ObjectMapper



/**
 * This plugin registers the web apis for the application.
 */
class DeepchainPlugin: WebServerPluginRegistry {
    override val webApis = listOf(Function(::GroupApi), Function(::ParamApi),
            Function(::OfficialApi), Function(::InfoApi))

    override fun customizeJSONSerialization(om: ObjectMapper) {}
}