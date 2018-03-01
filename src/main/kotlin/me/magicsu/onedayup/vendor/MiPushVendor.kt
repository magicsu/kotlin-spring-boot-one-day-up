package me.magicsu.onedayup.vendor

import com.xiaomi.xmpush.server.Message
import com.xiaomi.xmpush.server.Sender
import me.magicsu.onedayup.config.PushConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
object MiPushVendor {

    val SENDER = Sender(PushConfig.APP_SECRET)
    var log: Logger = LoggerFactory.getLogger(MiPushVendor::class.java)

    /**
     * 发送消息
     *
     * @param regId
     * @param content
     */
    fun sendMsg(regId: String, content: String) {
        try {
            val result = SENDER.send(buildMsg(content), regId, 3)
            log.info(" mi push result: {} ", result.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(Exception::class)
    private fun buildMsg(msgContent: String): Message {
        return Message.Builder()
                .payload(msgContent)
                .restrictedPackageName(PushConfig.PACKAGE_NAME)
                .passThrough(1)     // 消息使用透传方式
                .notifyType(-1)     // 使用默认提示音提示
                .build()
    }
}
