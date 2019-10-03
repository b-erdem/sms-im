package dev.erdem.smsim

import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

data class SmsConversation(val info: SmsConversationInfo, val messages: List<SmsMessage>)

class MainActivity : AppCompatActivity() {
    private var smsBroadcastReceiver = SmsBroadcastReceiver()
    private var smsBroadcastReceiverFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qr_code_button.setOnClickListener {
            val intent = Intent(this, QRCodeScannerActivity::class.java)
            startActivityForResult(intent, 1)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val channelId = data!!.getStringExtra("channel_id")!!
                SmsPublisherService.startJoinChannel(applicationContext, channelId)
                registerReceiver(smsBroadcastReceiver, smsBroadcastReceiverFilter)
            } else {
                Log.d("MainActivity", "Couldn't detect QR Code.")
            }
        }
    }

    override fun onDestroy() {
        Log.d("MainActivity", "destroyed")
        unregisterReceiver(smsBroadcastReceiver)
        SmsPublisherService.destroy(applicationContext)
        stopService(Intent(this, SmsPublisherService.javaClass))
        super.onDestroy()
    }
}
