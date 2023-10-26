package com.ingenieriajhr.testopencv

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ingenieriajhr.testopencv.databinding.ActivityMainBinding
import com.ingenieriiajhr.jhrCameraX.BitmapResponse
import com.ingenieriiajhr.jhrCameraX.CameraJhr
import org.opencv.android.OpenCVLoader
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var cameraJhr: CameraJhr
    lateinit var cascadeClassifier: CascadeClassifier

    lateinit var openUtils :OpenUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (OpenCVLoader.initDebug()) Log.d("OPENCV2023", "TRUE")
        else Log.d("OPENCV2023", "INCORRECTO")
        //init cameraJHR
        cameraJhr = CameraJhr(this)
        loadHaarCascadeModel()
        openUtils = OpenUtils(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (cameraJhr.allpermissionsGranted() && !cameraJhr.ifStartCamera) {
            startCameraJhr()
        } else {
            cameraJhr.noPermissions()
        }
    }

    private fun startCameraJhr() {
        cameraJhr.addlistenerBitmap(object : BitmapResponse {
            override fun bitmapReturn(bitmap: Bitmap?) {
                val newBitmap = openUtils.setUtil(bitmap!!,cascadeClassifier,applicationContext)
                if (bitmap!=null){
                    runOnUiThread {
                        binding.imgBitMap.setImageBitmap(newBitmap)
                    }
                }
            }
        })

        cameraJhr.initBitmap()
        //selector camera LENS_FACING_FRONT = 0;    LENS_FACING_BACK = 1;
        //aspect Ratio  RATIO_4_3 = 0; RATIO_16_9 = 1;  false returImageProxy, true return bitmap
        cameraJhr.start(1,0,binding.cameraPreview,true,false,true)
    }

    /**
     * @return bitmap rotate degrees
     */
    fun Bitmap.rotate(degrees:Float) = Bitmap.createBitmap(this,0,0,width,height,
        Matrix().apply { postRotate(degrees) },true)

    private fun loadHaarCascadeModel() {
        try {
            val `is`: InputStream = getApplicationContext().getAssets().open("haarcascade_frontalface_alt.xml")
            val cascadeDir = getDir("cascade", MODE_PRIVATE) // creating a folder
            val mCascadeFile =
                File(cascadeDir, "haarcascade_frontalface_alt.xml") // creating file on that folder
            val os = FileOutputStream(mCascadeFile)
            val buffer = ByteArray(4096)
            var byteRead: Int
            while (`is`.read(buffer).also { byteRead = it } != -1) {
                os.write(buffer, 0, byteRead)
            }
            `is`.close()
            os.close()
            cascadeClassifier = CascadeClassifier(mCascadeFile.absolutePath)
        } catch (e: IOException) {
            Log.i("HAARCASCADE", "Cascade file not found")
        }
    }
}