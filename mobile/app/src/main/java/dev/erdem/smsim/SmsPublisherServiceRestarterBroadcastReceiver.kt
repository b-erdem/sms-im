package dev.erdem.smsim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmsPublisherRestarterBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(context, SmsPublisherService::class.java))
    }
}
