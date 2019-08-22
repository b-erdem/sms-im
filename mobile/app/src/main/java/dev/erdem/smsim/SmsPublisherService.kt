package dev.erdem.smsim

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SmsPublisherService : Service() {
    private var smsSocket: SmsSocket? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        smsSocket = SmsSocket(SmsContentResolver(contentResolver))
        smsSocket!!.connect()
        smsSocket!!.joinChannel("room:lobby")

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("SmsPublisherService ", "started")
        val to = intent?.getStringExtra("to")
        val msg = intent?.getStringExtra("msg")

        if (msg != null) {
            Log.d("Channel Push message ", msg)
            smsSocket!!.pushMessage(to, msg)
        }

        return START_STICKY
    }


//    override fun onDestroy() {
//        super.onDestroy()
//        val broadcastIntent = Intent(this, SmsPublisherRestarterBroadcastReceiver::class.java)
//        sendBroadcast(broadcastIntent)
//    }
}