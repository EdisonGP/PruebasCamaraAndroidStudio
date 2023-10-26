package com.ingenieriajhr.testopencv

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class OpenUtils (activity: Activity){
    var imageSize = 100
    var clases: Map<String, List<Float>>? = null
    private lateinit var classifier: Classifier

    init{
        classifier = Classifier(activity)
    }

    fun setUtil(bitmap: Bitmap,cascadeClassifier: CascadeClassifier, context:Context):Bitmap{
        saveCroppedFaceImage(bitmap, context)
        val mat = Mat()
        Utils.bitmapToMat(bitmap,mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)
        val width = mat.width()
        val height = mat.height()
        //val absoluteFaceSize = (height * 0.1).toInt()
        val absoluteFaceSize = if (mat.width() > mat.height()) {
            (height * 0.1).toInt()
        } else {
            (width * 0.1).toInt()
        }
        val faces = MatOfRect()
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mat, faces, 1.1, 2, 2, Size(absoluteFaceSize.toDouble(), absoluteFaceSize.toDouble()), Size())
        }
        val facesArray: Array<Rect> = faces.toArray()
        for (i in facesArray.indices) {
            val faceRect: Rect = facesArray[i]
            Imgproc.rectangle(mat, faceRect.tl(), faceRect.br(), Scalar(0.0, 255.0, 0.0, 255.0), 2)
            val roi = Rect(
                (facesArray[i].tl().x).toInt(),
                (facesArray[i].tl().y).toInt(),
                (facesArray[i].br().x).toInt() - (facesArray[i].tl().x).toInt(),
                (facesArray[i].br().y).toInt() - (facesArray[i].tl().y).toInt()
            )
            val cropped = Mat(mat, roi)
            val croppedBitmap = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(cropped, croppedBitmap)
            val resizedCroppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, imageSize, imageSize, false)
           // saveCroppedFaceImage(resizedCroppedBitmap, context)
            val result = classifier.classify(resizedCroppedBitmap)
            val recognizedName: String? =  result.mPersonClass
            val textX = (faceRect.tl().x).toInt()
            val textY = (faceRect.tl().y).toInt() - 10
            Imgproc.putText(mat, recognizedName, Point(textX.toDouble(), textY.toDouble()), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 255.0, 0.0, 255.0), 1)

            // Agregar texto de probabilidad
            val probabilityText = "P: "+String.format("%.2f", result.mProbability)
            val PX = (faceRect.tl().x).toInt()+10
            val PY = (faceRect.tl().y).toInt() + 15
            Imgproc.putText(mat, probabilityText, Point(PX.toDouble(), PY.toDouble()), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(255.0, 255.0, 255.0), 1)
    }
      //
        Utils.matToBitmap(mat,bitmap)

        return bitmap
    }

    private fun saveCroppedFaceImage(croppedBitmap: Bitmap, context: Context) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())


        try {
            val imageFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "CroppedFace_$timeStamp.jpg"
            )
            val outputStream = FileOutputStream(imageFile)
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}