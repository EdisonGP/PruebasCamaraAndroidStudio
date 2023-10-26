package com.example.probandocamerax

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.util.Log

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis

import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.probandocamerax.databinding.ActivityCameraBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityCameraBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private val cameraXViewModel=viewModels<CameraXViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

        cameraXViewModel.value.processCameraProvider.observe(this){provider->
            processCameraProvider=provider
            bindCameraPreview()
            bindInputAnalyser()
        }
    }

    private fun bindCameraPreview(){
        cameraPreview=Preview.Builder()
            .setTargetRotation(binding.cameraPreview.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

        try {
            processCameraProvider.bindToLifecycle(this,cameraSelector,cameraPreview)
        }catch (illegalStateException: IllegalStateException){
            Log.e(TAG,illegalStateException.message?:"IllegalStateException")
        }catch (illegalArgumentException: IllegalArgumentException){
            Log.e(TAG,illegalArgumentException.message?:"IllegalArgumentException")
        }
    }

    private fun bindInputAnalyser(){
        val faceDetector = FaceDetection.getClient(
          FaceDetectorOptions.Builder()
              .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
              .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
              .build()
        )

        imageAnalysis=ImageAnalysis.Builder()
            .setTargetRotation(binding.cameraPreview.display.rotation)
            .build()

        val cameraExecutor=Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor){imageProxy->
            processImageProxy(faceDetector,imageProxy)
        }

        processCameraProvider.bindToLifecycle(this,cameraSelector,imageAnalysis)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(faceDetector: FaceDetector,imageProxy: ImageProxy){
        val inputImage=InputImage.fromMediaImage(imageProxy.image!!,imageProxy.imageInfo.rotationDegrees)

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces->
                binding.faceBoxOverlay.clear()

                faces.forEach{face->
                    println("image Proxy")
                    println(imageProxy.cropRect)
                    println("bounding box")
                    println(face.boundingBox)
                    val box=FaceBox(binding.faceBoxOverlay,face,imageProxy.cropRect)
                    binding.faceBoxOverlay.add(box)
                }

            }.addOnFailureListener{
                Log.e(TAG,it.message?:it.toString())
            }.addOnCanceledListener {
                imageProxy.close()
            }
    }

    companion object{
        private val TAG=CameraActivity::class.simpleName

        fun startFaceDetection(context: Context){
            Intent(context,CameraActivity::class.java).also{
                context.startActivity(it)
            }
        }
    }






    /*private fun loadHaarCascadeModel() {
        try {
            val `is` = applicationContext.assets.open("haarcascade_frontalface_alt.xml")
            val cascadeDir = getDir("cascade", MODE_PRIVATE) // creating a folder
            val mCascadeFile =
                File(cascadeDir, "haarcascade_frontalface_alt.xml") // creating file on that folder
            val os = FileOutputStream(mCascadeFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (`is`.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
            }
            `is`.close()
            os.close()
          //  cascadeClassifier = CascadeClassifier(mCascadeFile.absolutePath)
            Log.i("HAARCASCADE", "Cascade file load success")
        } catch (e: IOException) {
            Log.i("HAARCASCADE", "Cascade file not found")
        }
    }

    private val bitmap: Bitmap? = null
    private fun processImage(imageProxy: ImageProxy) {
        val bitmap = convertImageProxyToBitmap(imageProxy)
        if (bitmap != null) {
           // faceDetector.setImageProxy(imageProxy)
          //  faceDetector.setUtil(bitmap, cascadeClassifier, this, faceBoxOverlay)
            /*  runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    image.setImageBitmap(newBitmap);
                }
            });*/
        }
    }

    private fun convertImageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uvBuffer = imageProxy.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uvSize = uvBuffer.remaining()
        val nv21 = ByteArray(ySize + uvSize)
        yBuffer[nv21, 0, ySize]
        uvBuffer[nv21, ySize, uvSize]
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, outputStream)
        val jpegData = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
    }*/
}