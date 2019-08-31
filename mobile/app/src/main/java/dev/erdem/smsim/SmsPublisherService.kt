package dev.erdem.smsim

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import org.phoenixframework.Channel
import org.phoenixframework.Payload
import org.phoenixframework.Socket

private const val ACTION_JOIN_CHANNEL = "dev.erdem.smsim.action.JOIN_CHANNEL"
private const val ACTION_PUBLISH_SMS = "dev.erdem.smsim.action.PUBLISH_SMS"

private const val EXTRA_CHANNEL_ID = "dev.erdem.smsim.extra.CHANNEL_ID"
private const val EXTRA_FROM = "dev.erdem.smsim.extra.FROM"
private const val EXTRA_TO = "dev.erdem.smsim.extra.TO"
private const val EXTRA_BODY = "dev.erdem.smsim.extra.BODY"
private const val EXTRA_TIMESTAMP = "dev.erdem.smsim.extra.TIMESTAMP"

class SmsPublisherService : IntentService("SmsPublisherService") {
    private val socket = Socket("http://104.248.20.26:4000/socket", mapOf())
    private var channel: Channel? = null
    private var smsContentResolver: SmsContentResolver? = null

    override fun onCreate() {
        Log.d("SmsPublisherService", "onCreate")
        smsContentResolver = SmsContentResolver(contentResolver)
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_JOIN_CHANNEL -> {
                val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)!!
                handleJoinChannel(channelId)
            }
            ACTION_PUBLISH_SMS -> {
                val from = intent.getStringExtra(EXTRA_FROM)!!
                val body = intent.getStringExtra(EXTRA_BODY)!!
                val timestamp = intent.getStringExtra(EXTRA_TIMESTAMP)!!
                handlePublishSms(from, body, timestamp)
            }
        }
    }

    private fun handlePublishSms(from: String, body: String, timestamp: String) {
        Log.d("handlePublishSms", body)
        channel?.push("new_msg", mapOf("body" to body, "from" to from, "timestamp" to timestamp))
    }

    private fun handleJoinChannel(channelId: String) {
        Log.d("handleJoinChannel", channelId)
        socket.connect()
        val channel = socket.channel("room:$channelId", mapOf("mobile" to true))
        channel.on("join") {
            Log.d("Channel", "joined")
        }

        channel.on("send_sms") { message ->
            Log.d("send_sms", "got new message")
            val payload = message.payload
            val to = payload["to"]
            val msg = payload["message"]
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(msg as String)
            smsManager.sendMultipartTextMessage(to as String?, null, parts, null, null)
        }

        channel.on("last_10_messages") {
            Log.d("last_10_messages", "requested")

            val payload = mutableMapOf<String, MutableList<SmsConversation>>()
            payload["conversations"] = mutableListOf()
            Log.d("last_10_messages", "requested")
            val conversations = smsContentResolver?.getConversations()
            Log.i("conversations length", conversations?.size.toString())
            if (conversations != null) {
                for (conversation in conversations) {
                    val messages = smsContentResolver?.getMessagesByThreadId(conversation.thread_id)
                    Log.i("messages length", messages?.size.toString())
                    payload["conversations"]!!.add(SmsConversation(info = conversation, messages = messages!!))
                }
            }

            channel.push("last_10", payload)
        }

        channel.on("more_messages") { message ->
            Log.d("more_messages", "requested")
            val threadId = message.payload["threadId"] as String
            val position = message.payload["position"] as Int
            val count = message.payload["count"] as Int

            val messages = smsContentResolver?.getMessagesByThreadId(threadId, position, count)

            channel.push("more_messages_reply",
                mapOf(threadId to messages!!)
            )
        }

        channel.on("thread_by_phone") { message ->
            val phoneNumber = message.payload["phoneNumber"] as String

        }

        channel.join().receive("ok") {
            Log.d("Channel", "successfully joined.")
        }

        this.channel = channel
    }

    companion object {
        @JvmStatic
        fun pushMessage(context: Context, from: String?, body: String?, timestamp: String?) {
            Log.d("pushMessage", "$from $body $timestamp")
            val intent = Intent(context, SmsPublisherService::class.java).apply {
                action = ACTION_PUBLISH_SMS
                putExtra(EXTRA_FROM, from)
                putExtra(EXTRA_BODY, body)
                putExtra(EXTRA_TIMESTAMP, timestamp)
            }
            context.startService(intent)
        }

        @JvmStatic
        fun startJoinChannel(context: Context, channelId: String) {
            val intent = Intent(context, SmsPublisherService::class.java).apply {
                action = ACTION_JOIN_CHANNEL
                putExtra(EXTRA_CHANNEL_ID, channelId)
            }
            context.startService(intent)
        }
    }
}
