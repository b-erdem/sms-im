package dev.erdem.smsim

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log


class SmsBroadcastReceiver : BroadcastReceiver() {
    init {
        Log.d("SMSBroadcastReceiver", "constructed.")
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Receiver: ", "onReceive Method Called.")

        if (resultCode == Activity.RESULT_OK) {
            val data = intent.extras
            val pdus = data!!.get("pdus") as Array<Any>?
            for (i in pdus!!.indices) {
                val smsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                val smsPublisherServiceIntent = Intent(context, SmsPublisherService::class.java)
                smsPublisherServiceIntent.putExtra("from", smsMessage.emailFrom) /* put id of sms */
                smsPublisherServiceIntent.putExtra("to", smsMessage.displayOriginatingAddress)
                smsPublisherServiceIntent.putExtra("msg", smsMessage.messageBody)
                context.startService(smsPublisherServiceIntent)
            }
        }
    }
}
