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
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    internal lateinit var mServiceIntent: Intent
    private var smsPublisherService: SmsPublisherService? = null

    private var cameraSource: CameraSource? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val detector = BarcodeDetector.Builder(applicationContext).setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE).build()
//
//        if (!detector.isOperational()) {
//            Toast.makeText(applicationContext, "CAnnot read qr code.", Toast.LENGTH_LONG).show()
//            return
//        }

        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS)
        requestPermissions(permissions, 1)

//
//        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    Activity#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for Activity#requestPermissions for more details.
//            Log.e("Permissions", "are not granted")
//            return
//        }

        cameraSource = CameraSource(this, fireFaceOverlay)

        cameraSource?.setMachineLearningFrameProcessor(BarcodeScanningProcessor())
        cameraSource?.let {
            try {
                Log.i("cameraSource", "before start")
                firePreview?.start(cameraSource!!, fireFaceOverlay)
            } catch (e: IOException) {
                Log.e("BarCode","Unable to start camera")
                cameraSource?.release()
                cameraSource = null
            }
        }

        smsPublisherService = SmsPublisherService(/*getApplicationContext() */)
        mServiceIntent = Intent(applicationContext, smsPublisherService!!.javaClass)

        if (!isSmsServiceRunning(smsPublisherService!!.javaClass)) {
            startService(mServiceIntent)
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
