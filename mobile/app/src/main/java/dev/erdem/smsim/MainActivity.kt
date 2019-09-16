package dev.erdem.smsim

import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

data class SmsConversation(val info: SmsConversationInfo, val messages: List<SmsMessage>)

class MainActivity : AppCompatActivity() {
    private lateinit var mServiceIntent: Intent
    private var smsPublisherService: SmsPublisherService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qr_code_button.setOnClickListener {
            val intent = Intent(this, QRCodeScannerActivity::class.java)
            startActivityForResult(intent, 1)
        }

        smsPublisherService = SmsPublisherService(/*getApplicationContext() */)
        mServiceIntent = Intent(applicationContext, smsPublisherService!!.javaClass)

        if (!isSmsServiceRunning(smsPublisherService!!.javaClass)) {
            startService(mServiceIntent)
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
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val channelId = data!!.getStringExtra("channel_id")!!
                SmsPublisherService.startJoinChannel(applicationContext, channelId)
            } else {
                Log.d("MainActivity.onActivityResult", "Couldn't detect QR Code.")
            }
        }
    }

    private fun isSmsServiceRunning(serviceClass: Class<*>): Boolean {
        val mgr = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (serviceInfo in mgr.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == serviceInfo.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        stopService(mServiceIntent)
        super.onDestroy()
    }
}
