package dev.erdem.smsim

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_qrcode_scanner.*

class QRCodeScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scanner)

        requestPermissions(arrayOf(Manifest.permission.CAMERA), 2)

        if (checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            return
        }

        val barcodeDetector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE).build()
        val cameraSource = CameraSource.Builder(applicationContext, barcodeDetector).setRequestedPreviewSize(264, 264).setAutoFocusEnabled(true).build()

        surfaceView.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                cameraSource.stop()
            }

            override fun surfaceCreated(p0: SurfaceHolder?) {
                if(checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
                    return
                }
                cameraSource.start(surfaceView.holder)
            }
        })

        if (barcodeDetector.isOperational) {
            barcodeDetector.setProcessor(object: Detector.Processor<Barcode> {
                override fun release() {
                }

                override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                    val barcodes = detections!!.detectedItems
                    if (barcodes.size() > 0) {
                        Log.d("qrcode", "detected")
                        val channelId = barcodes.valueAt(0).displayValue.toString()
                        Log.d("QRCODE", barcodes.valueAt(0).displayValue.toString())
                        barcodeDetector.release()

                        val returnIntent = Intent()
                        returnIntent.putExtra("channel_id", channelId)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                }
            })
        } else {
            Log.d("QRCodeScannerActivity", "Barcode detector is not operational.")

            setResult(Activity.RESULT_CANCELED, Intent())
        }


    }
}
