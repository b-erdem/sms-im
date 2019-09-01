package dev.erdem.smsim

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.provider.Telephony

class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Receiver: ", "onReceive Method Called.")

        if (resultCode == Activity.RESULT_OK) {
            val data = intent.extras
            val pdus = data!!.get("pdus") as Array<*>?
            for (i in pdus!!.indices) {
                val smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent)[i]
                SmsPublisherService.pushMessage(context, from = smsMessage.originatingAddress, body = smsMessage.messageBody, timestamp = smsMessage.timestampMillis.toString())
            }
        }
    }
}
