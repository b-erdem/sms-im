package dev.erdem.smsim

import android.app.Service
import android.content.Intent
import android.content.Context
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import org.phoenixframework.Channel
import org.phoenixframework.Socket

private const val ACTION_JOIN_CHANNEL = "dev.erdem.smsim.action.JOIN_CHANNEL"
private const val ACTION_PUBLISH_SMS = "dev.erdem.smsim.action.PUBLISH_SMS"

private const val EXTRA_CHANNEL_ID = "dev.erdem.smsim.extra.CHANNEL_ID"
private const val EXTRA_FROM = "dev.erdem.smsim.extra.FROM"
private const val EXTRA_TO = "dev.erdem.smsim.extra.TO"
private const val EXTRA_BODY = "dev.erdem.smsim.extra.BODY"
private const val EXTRA_TIMESTAMP = "dev.erdem.smsim.extra.TIMESTAMP"

class SmsPublisherService : Service() {
    private val socket = Socket("https://websms-backend.erdem.dev:8443/socket", mapOf())
    private var channel: Channel? = null
    private var smsContentResolver: SmsContentResolver? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d("SmsPublisherService", "onCreate")
        smsContentResolver = SmsContentResolver(contentResolver)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

        return START_STICKY
    }

    private fun handlePublishSms(from: String, body: String, timestamp: String) {
        Log.d("handlePublishSms", body)
        channel!!.push("new_msg", mapOf("body" to body, "from" to from, "timestamp" to timestamp))
    }

    private fun handleJoinChannel(channelId: String) {
        Log.d("handleJoinChannel", channelId)
        socket.connect()
        val channel = socket.channel("room:$channelId", mapOf("mobile" to true, "device" to "${Build.MANUFACTURER} ${Build.MODEL}"))
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

        channel.on("recent_conversations") {
            Log.d("recent_conversations", "requested")

            var position = 0

            if (it.payload["position"] as? Double != null) {
                position = (it.payload["position"] as? Double)!!.toInt()
            }

            val payload = mutableMapOf<String, MutableList<SmsConversation>>()
            payload["conversations"] = mutableListOf()
            val conversations = smsContentResolver?.getConversations(position)
            Log.i("conversations length", conversations?.size.toString())
            if (conversations != null) {
                for (conversation in conversations) {
                    val messages = smsContentResolver?.getMessagesByThreadId(conversation.thread_id)
                    Log.i("messages length", messages?.size.toString())
                    payload["conversations"]!!.add(SmsConversation(info = conversation, messages = messages!!))
                }
            }

            channel.push("recent_conversations", payload)
        }

        channel.on("more_messages") {
            Log.d("more_messages", "requested")
            val payload = it.payload
            val threadId = payload["threadId"] as? String
            val position = payload["position"] as? Double

            Log.i("more_messages_info", "threadId $threadId position $position")

            val messages = smsContentResolver?.getMessagesByThreadId(threadId!!, position!!.toInt())
            Log.i("more_messages", messages.toString())

            channel.push("more_messages",
                mapOf(threadId!! to messages!!)
            )
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
