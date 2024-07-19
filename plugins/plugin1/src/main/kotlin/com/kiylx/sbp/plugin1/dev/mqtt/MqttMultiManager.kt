package com.kiylx.sbp.plugin1.dev.mqtt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import lombok.extern.slf4j.Slf4j
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.springframework.stereotype.Component

@Slf4j
@Component
class MqttMultiManager {
    val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    val clients: MutableMap<String, MqttClientWrapper> = mutableMapOf()
    var persistence: MemoryPersistence = MemoryPersistence()

    fun getClient(clientId:String): MqttClientWrapper {
        return clients[clientId] ?: throw RuntimeException("clientId:$clientId not found")
    }

    /**
     *新建一个客户端连接服务端
     * @param info
     */
    fun newClient2ConnectServer(info: MqttConnectRequestInfo): MqttClientWrapper {
        return clients[info.clientId] ?: let {
            val clientWrapper = MqttClientWrapper(
                info.hostUrl, info.clientId,
                persistence = persistence
            )
            val connOption = MqttConnectOptions().apply {
                isCleanSession = true
                keepAliveInterval = 180
                isAutomaticReconnect = true
                maxInflight = 120 // PublishMultiple overwhelms Paho defaults 增大可同时发送消息数量
                userName = info.username
                password = info.password.toCharArray()
            }
            clientWrapper.scope = coroutineScope
            clientWrapper.connOption = connOption
            clients[info.clientId] = clientWrapper
            clientWrapper
        }
    }

    fun removeClient(clientId: String) {
        clients.remove(clientId)?.let {
            it.disconnect()
            it.close()
        }
    }
}