package com.kiylx.sbp.plugin1.dev.mqtt


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.internal.ClientComms
import java.util.concurrent.ScheduledExecutorService
import kotlin.time.Duration.Companion.seconds

class TestMqtt{
    companion object{
        fun main() {
            //示例：
            runBlocking {
                //这个在spring中使用了@Component注解，是单例。因为演示，直接new了一个实例
                val info = MqttConnectRequestInfo(
                    "tcp://localhost:1883",
                    "guest", "guest",
                    "sub-cl501nt-id-9v83po7c-dev"
                )

                MqttMultiManager().newClient2ConnectServer(info).scope {
                    config {
                        //修改连接配置

                    }
                    print("执行连接")
                    connect(object : MqttCallback {
                        override fun connectionLost(cause: Throwable?) {
                            print("connectionLost")
                        }

                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            print("connect topic:$topic" + "+ msg:${String(message!!.payload)}\n")
                        }

                        override fun deliveryComplete(token: IMqttDeliveryToken?) {
                            print("发送完成")
                        }
                    })
                    subscribe(object : SubscribeListener(topicFilter = "topic1", 0) {
                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            print("subscribe topic:$topic" + "+ msg:${String(message!!.payload)}\n")
                            publish("test","success")
                        }
                    })
                }
                delay(60.seconds)
            }

        }
    }
}

class MqttClientWrapper : MqttClient {
    lateinit var connOption: MqttConnectOptions
    lateinit var scope: CoroutineScope

    constructor(info: MqttConnectRequestInfo, persistence: MqttClientPersistence) :
            this(info.hostUrl, info.clientId, persistence = persistence)

    constructor(serverURI: String, clientId: String) : super(serverURI, clientId)
    constructor(
        serverURI: String, clientId: String,
        persistence: MqttClientPersistence
    ) : super(serverURI, clientId, persistence)

    constructor(
        serverURI: String, clientId: String,
        persistence: MqttClientPersistence, executorService: ScheduledExecutorService
    ) : super(serverURI, clientId, persistence, executorService)

    /**
     * 配置连接属性，需要在[connect]之前调用
     *
     * @param build
     * @receiver
     */
    fun config(build: MqttConnectOptions.() -> Unit) {
        if (isConnected)
            return
        connOption.build()
    }

    fun scope(func: suspend MqttClientWrapper.() -> Unit) {
        scope.launch {
            func.invoke(this@MqttClientWrapper)
        }
    }

    suspend fun connect(callback: MqttCallback?=null): MqttClientWrapper {
        callback?.let {
            super.setCallback(callback)
        }
        super.connect(this.connOption)
        return this
    }

    suspend fun subscribe(vararg listeners: SubscribeListener): MqttClientWrapper {
        val topicFilters = mutableListOf<String>()
        val qosValue = mutableListOf<Int>()
        listeners.forEach {
            topicFilters += it.topicFilter
            qosValue += it.qos
        }
        super.subscribe(topicFilters.toTypedArray(), qosValue.toIntArray(), listeners)
        return this
    }

    fun unSubscribe(vararg topicFilter: String) {
        super.unsubscribe(topicFilter)
    }

    /**
     * Publish
     *
     * @param topic
     * @param msg
     * @param qos QoS 0，最多交付一次。QoS 1，至少交付一次。QoS 2，只交付一次（rabbitmq目前不支持 qos为2）。
     * @param retained 消息传递引擎是否应保留发布消息。
     *      发送一条消息，将保留设置为 true，
     *      并以空字节数组作为有效负载，例如 new byte[0]，将从服务器中清除保留的消息。默认值为 false
     */
    fun publish(
        topic: String,
        msg: String,
        qos: Int = 0,
        retained: Boolean = false
    ) {
        scope.launch {
            super.publish(topic, msg.toByteArray(Charsets.UTF_8), qos, retained)
        }
    }

    @Volatile
    var reflectionComms: ClientComms? = null

    fun clientComms(): ClientComms? {
        return reflectionComms ?: synchronized(this) {
            reflectionComms ?: let {
                val cls = super.aClient.javaClass
                val field = cls.getDeclaredField("comms")
                field.isAccessible = true
                reflectionComms = field.get(super.aClient) as ClientComms?
                reflectionComms
            }
        }
    }

    val isConnecting get() = clientComms()?.isConnecting ?: false

    val isDisconnecting get() = clientComms()?.isDisconnecting ?: false

    val isDisconnected get() = clientComms()?.isDisconnected ?: true

    val isClosed get() = clientComms()?.isClosed ?: true


}

/**
 * Subscribe listener
 *
 * 调用subscribe时传递的callback，跟此处一样，也有一个[messageArrived]方法
 * 区别在于：订阅时传递的callback专门处理某topic的消息。
 * 而这里的[messageArrived]方法，是一个默认处理功能，用于处理没被其他订阅的消息
 * 内部调用的地方的注释：
 * if the message hasn't been delivered to a per subscription handler, give it to the default handler
 *
 * @property topicFilter
 * @property qos
 * @constructor Create empty Subscribe listener
 */
abstract class SubscribeListener(
    val topicFilter: String,
    val qos: Int = 0,
) : IMqttMessageListener