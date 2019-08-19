package dev.erdem.smsim

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import org.phoenixframework.Channel
import org.phoenixframework.Socket

class SmsPublisherService : Service() {
    private var socket: Socket? = null
    private var channel: Channel? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return null
    }

    override fun onCreate() {
        socket = Socket("http://192.168.1.8:4000/socket", mapOf())
        socket!!.connect()
        Log.d("Socket", "connected")

        channel = socket!!.channel("room:lobby")
        channel!!.on("join") {
            Log.d("Channel", "joined")
        }
        channel!!.on("new_msg") { message ->
            Log.d("new_msg", "got new message")
            val payload = message.payload
            Log.d("new_msg payload ", message.payload.keys.toString())
            val to = payload["to"]
            val msg = payload["message"]
            Log.d("new_msg to ", to as String)
            Log.d("new_msg message ", msg.toString())
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(to as String?, null, msg as String?, null, null)
        }
        channel!!.on("last_10_messages") {
            val payload = mutableMapOf<String, MutableList<Map<String, String>>>()

            Log.d("last_10_messages", "requested")
            val conversations = contentResolver.query(Telephony.Sms.Conversations.CONTENT_URI, null, null, null, null)

            if (conversations != null) {
                Log.d("conversations count", conversations.count.toString())
                while (conversations.moveToNext()) {
                    val threadId = conversations.getString(conversations.getColumnIndex("thread_id"))
                    val messages = contentResolver.query(Telephony.Sms.Conversations.CONTENT_URI.buildUpon().appendPath(threadId).build(), null, null, null, null)
                    while (messages!!.moveToNext()) {
                        val address = messages.getString(messages.getColumnIndex("address"))
                        val creator = messages.getString(messages.getColumnIndex("creator"))
                        val date = messages.getString(messages.getColumnIndex("date"))
                        val body = messages.getString(messages.getColumnIndex("body"))
                        val type = messages.getString(messages.getColumnIndex("type"))
                        payload.putIfAbsent(address, mutableListOf())
                        payload[address]!!.add(mapOf("address" to address, "body" to body, "date" to date, "creator" to creator, "type" to type))
                    }
                }

                channel?.push("last_10", payload)
            }
        }
        channel!!.join().receive("ok") {
            Log.d("Channel", "successfully joined.")
        }
        Log.d("Channel", "created")

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("SmsPublisherService ", "started")
        val from = intent?.getStringExtra("from")
        val to = intent?.getStringExtra("to")
        val msg = intent?.getStringExtra("msg")

        if (msg != null) {
            Log.d("channel", "push")
            Log.d("Channel Push message ", msg)
            val payload = mapOf("to" to to!!, "message" to msg!!)
            channel?.push("new_msg", payload)
        }

        return START_STICKY
    }


//    override fun onDestroy() {
//        super.onDestroy()
//        val broadcastIntent = Intent(this, SmsPublisherRestarterBroadcastReceiver::class.java)
//        sendBroadcast(broadcastIntent)
//    }
}