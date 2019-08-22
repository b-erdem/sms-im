package dev.erdem.smsim

import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    private lateinit var mServiceIntent: Intent
    private var smsPublisherService: SmsPublisherService? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        smsPublisherService = SmsPublisherService(/*getApplicationContext() */)
        mServiceIntent = Intent(applicationContext, smsPublisherService!!.javaClass)

        if (!isSmsServiceRunning(smsPublisherService!!.javaClass)) {
            startService(mServiceIntent)
        }

        val permissions = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS)
        requestPermissions(permissions, 1)


        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return
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
