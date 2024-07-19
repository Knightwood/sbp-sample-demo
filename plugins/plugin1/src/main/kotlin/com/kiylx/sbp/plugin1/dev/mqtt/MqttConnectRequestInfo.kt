package com.kiylx.sbp.plugin1.dev.mqtt


data class MqttConnectRequestInfo(
    val hostUrl: String,
    val username: String,
    val password: String,
    val clientId:String,
    val pubClientId: String? = null,
    val subClientId: String? = null,
    val pubTopic: String? = null,
    val subTopic: String? = null,
    val completionTimeout: Int = 0,
)