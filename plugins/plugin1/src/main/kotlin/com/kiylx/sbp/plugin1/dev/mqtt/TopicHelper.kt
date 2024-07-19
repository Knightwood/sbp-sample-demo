package com.kiylx.sbp.plugin1.dev.mqtt

class TopicHelper {
    companion object {
        const val statusStr = "device/status/"
        const val commandStr = "device/command/"

        const val subscribe="subscribe"
        const val publish="publish"

        const val deny = "deny"
        const val allow = "allow"
        const val ignore = "ignore"

        fun clientSn(string: String): String {
            return string.split("/".toRegex())[1]
        }

        fun deviceMode(string: String): String {
            return string.split("/".toRegex())[2]
        }

        /**
         *将topic数组字符串拆分，获得topic列表
         * 例如 "topic1,topic2,topic3"
         * 返回 ["topic1","topic2","topic3"]
         */
        fun definedTopics(string: String): Array<String> = string.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

    }
}
