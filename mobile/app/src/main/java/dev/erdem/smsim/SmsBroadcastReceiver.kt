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
                // context.startService()
                val returnIntent = Intent("dev.erdem.smsim.SmsReceiver")
                returnIntent.putExtra("from", smsMessage.emailFrom)
                returnIntent.putExtra("to", smsMessage.displayOriginatingAddress)
                returnIntent.putExtra("body", smsMessage.messageBody)
                returnIntent.putExtra("timestamp", smsMessage.timestampMillis)
                context.sendBroadcast(returnIntent)
            }
        }
    }
}
