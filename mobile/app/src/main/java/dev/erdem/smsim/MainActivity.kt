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
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var smsSocket = SmsSocket(SmsContentResolver(contentResolver))

    private var broadcastReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (smsSocket.isJoinedChannel) {
                val to = intent?.getStringExtra("to")!!
                val from = intent.getStringExtra("from")!!
                val body = intent.getStringExtra("body")!!
                val timestamp = intent.getStringExtra("timestamp")!!
                smsSocket.pushMessage(to, body, timestamp)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qr_code_button.setOnClickListener {
            val intent = Intent(this, QRCodeScannerActivity::class.java)
            startActivityForResult(intent, 1)
        }

        setupSocket()

        val intentFilter = IntentFilter()
        intentFilter.addAction("dev.erdem.smsim.SmsReceiver")
        registerReceiver(broadcastReceiver, intentFilter)

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
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val channelId = data!!.getStringExtra("channel_id")!!
                connectChannel(channelId)
            } else {
                Log.d("MainActivity.onActivityResult", "Couldn't detect QR Code.")
            }
        }
    }

    private fun setupSocket() {
        smsSocket.connect()
    }

    private fun connectChannel(channelId: String) {
        smsSocket.joinChannel(channelId)
    }

    override fun onStop() {
        super.onStop()

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("dev.erdem.smsim.SmsReceiver")
        registerReceiver(broadcastReceiver, intentFilter)
    }
}
