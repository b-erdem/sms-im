package dev.erdem.smsim

import android.telephony.SmsManager
import android.util.Log
import org.phoenixframework.Channel
import org.phoenixframework.Socket

data class SmsConversation(val info: SmsConversationInfo, val messages: List<SmsMessage>)

class SmsSocket(private val smsContentResolver: SmsContentResolver) {
    var isJoinedChannel: Boolean = false
    private var socket : Socket = Socket("http://104.248.20.26:4000/socket", mapOf())
    private var channel : Channel? = null

    fun connect() {
        socket.connect()
    }

    fun joinChannel(channelId: String) {
        val channel = socket.channel(channelId)
        channel.on("join") {
            Log.d("Channel", "joined")
        }

        channel.on("send_sms") { message ->
            Log.d("send_sms", "got new message")
            val payload = message.payload
            val to = payload["to"]
            val msg = payload["message"]
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(to as String?, null, msg as String?, null, null)
        }

        channel.on("last_10_messages") {
            Log.d("last_10_messages", "requested")

            val payload = mutableMapOf<String, MutableList<SmsConversation>>()
            payload["conversations"] = mutableListOf()
            Log.d("last_10_messages", "requested")
            val conversations = smsContentResolver.getConversations()
            Log.i("conversations length", conversations.size.toString())
            for (conversation in conversations) {
                val messages = smsContentResolver.getMessagesByThreadId(conversation.thread_id)
                Log.i("messages length", messages.size.toString())
                payload["conversations"]!!.add(SmsConversation(info = conversation, messages = messages))
            }

            channel.push("last_10", payload)
        }

        channel.join().receive("ok") {
            Log.d("Channel", "successfully joined.")
            isJoinedChannel = true
        }

        this.channel = channel
    }

    fun pushMessage(to: String, body: String, timestamp: String) {
        channel?.push("new_msg", mapOf("body" to body, "to" to to, "timestamp" to timestamp))
    }
}
