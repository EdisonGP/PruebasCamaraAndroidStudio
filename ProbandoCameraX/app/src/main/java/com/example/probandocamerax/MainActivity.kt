package com.example.probandocamerax

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.probandocamerax.databinding.ActivityCameraBinding
import com.example.probandocamerax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val cameraPermission = android.Manifest.permission.CAMERA
    private  lateinit var binding : ActivityMainBinding

    private val  requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted->
        if(isGranted){
            startFaceDetector()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openCamera.setOnClickListener(){
           // reguestCameraAndStartScanner()
            CameraActivity.startFaceDetection(this)
        }
    }

    private fun reguestCameraAndStartScanner(){
        if(isPermissionGranted(cameraPermission)){
            startFaceDetector()
        }else{
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission(){
        when{
            shouldShowRequestPermissionRationale(cameraPermission)->{
                cameraPermissionRequest {
                    openPermissionSettings()
                }
            }else->{
            requestPermissionLauncher.launch(cameraPermission)
        }
        }
    }

    private fun startFaceDetector(){
        CameraActivity.startFaceDetection(this)
    }
}