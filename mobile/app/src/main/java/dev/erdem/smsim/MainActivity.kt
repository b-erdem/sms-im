package dev.erdem.smsim

import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.phoenixframework.Channel
import org.phoenixframework.Socket

data class SmsConversation(val info: SmsConversationInfo, val messages: List<SmsMessage>)

class MainActivity : AppCompatActivity() {
    private val socket = Socket("http://104.248.20.26:4000/socket", mapOf())
    private var channel: Channel? = null
    private var isJoinedChannel = false
    private var smsContentResolver: SmsContentResolver? = null

    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qr_code_button.setOnClickListener {
            val intent = Intent(this, QRCodeScannerActivity::class.java)
            startActivityForResult(intent, 1)
        }

        smsContentResolver = SmsContentResolver(contentResolver)

        setupLocalBroadcastReceiver()
        setupSocket()

        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        )
        requestPermissions(permissions, 1)

        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            return
        }
    }

    private fun setupLocalBroadcastReceiver() {
        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (isJoinedChannel) {
                    val to = intent?.getStringExtra("to")!!
                    val from = intent.getStringExtra("from")!!
                    val body = intent.getStringExtra("body")!!
                    val timestamp = intent.getStringExtra("timestamp")!!
                    channel?.push("new_msg", mapOf("body" to body, "to" to to, "timestamp" to timestamp))
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction("dev.erdem.smsim.SmsReceiver")
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val channelId = data!!.getStringExtra("channel_id")!!
                setupChannel(channelId)
            } else {
                Log.d("MainActivity.onActivityResult", "Couldn't detect QR Code.")
            }
        }
    }

    private fun setupSocket() {
        socket.connect()
    }

    private fun setupChannel(channelId: String) {
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
            smsManager.sendTextMessage(to as String?, null, msg as String?, null, null)
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

        channel.join().receive("ok") {
            Log.d("Channel", "successfully joined.")
            isJoinedChannel = true
        }

        this.channel = channel
    }
}
