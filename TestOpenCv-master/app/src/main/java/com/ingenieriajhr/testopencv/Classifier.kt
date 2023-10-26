package com.ingenieriajhr.testopencv

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class Classifier(activity: Activity) {

    companion object {
        const val MODEL_NAME = "cnn_face_recognition.tflite"
        const val BATCH_SIZE = 1 // We'll display only one image
        const val IMAGE_HEIGHT = 100 // Pixel's Height of the image
        const val IMAGE_WIDTH = 100 // Pixel's Width of the image
        const val NUM_CHANNEL = 1 // Color Channel (Green Channel)
        const val NUM_CLASSES = 16 // Num Labels - Digits from 0 to 16
        const val JSON_FILE_NAME = "known_data.json" // JSON file with ground truth embeddings
        const val THRESHOLD = 0.5f // Threshold for Euclidean distance
    }

    private val options = Interpreter.Options()
    private lateinit var interpreter: Interpreter
    private lateinit var imageData: ByteBuffer
    private val imagePixels = IntArray(IMAGE_HEIGHT * IMAGE_WIDTH)

    private val result = Array(1){ FloatArray(NUM_CLASSES) }
    private val groundTruthEmbeddings: Map<String, FloatArray> = loadGroundTruthEmbeddings(activity)

    init {
        try {
            interpreter = Interpreter(loadModelFile(activity), options)
            imageData = ByteBuffer.allocateDirect(4 * BATCH_SIZE * IMAGE_HEIGHT * IMAGE_WIDTH * NUM_CHANNEL)
            imageData.order(ByteOrder.nativeOrder())
            loadModelFile(activity)
            Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
        } catch (exception: Exception) {
            Toast.makeText(activity, "Error cargando modelo de ML", Toast.LENGTH_SHORT).show()
            Toast.makeText(activity, "Exception: $exception", Toast.LENGTH_SHORT).show()
        }
        imageData = ByteBuffer.allocateDirect(4 * BATCH_SIZE * IMAGE_HEIGHT * IMAGE_WIDTH * NUM_CHANNEL)
        imageData.order(ByteOrder.nativeOrder())
    }

    @Throws(IOException::class)
    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val assetFileDescriptor: AssetFileDescriptor = activity.assets.openFd(MODEL_NAME)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val length = assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }

    @Throws(IOException::class)
    fun loadGroundTruthEmbeddings(context: Context): Map<String, FloatArray> {
        val classes: MutableMap<String, FloatArray> = HashMap()
        try {
            val `is` = context.assets.open("known_data.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            val json = kotlin.String()
            val jsonObject = JSONObject(json)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val jsonArray = jsonObject.getJSONArray(key)
                val values: MutableList<Float> = ArrayList()
                for (i in 0 until jsonArray.length()) {
                    values.add(jsonArray.getDouble(i).toFloat())
                }
                classes[key] = values.toFloatArray()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return classes
    }

    fun classify(bitmap: Bitmap): Result {
        convertBitmapToByteBuffer(bitmap)
        interpreter.run(imageData, result)

        return Result(result[0],0L)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imageData.rewind()
        bitmap.getPixels(imagePixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until IMAGE_WIDTH) {
            for (j in 0 until IMAGE_HEIGHT) {
                val value: Int = imagePixels[pixel++]
                imageData.putFloat(convertPixel(value))
            }
        }
    }

    private fun convertPixel(color: Int): Float {
        return (255 - ((color shr 16 and 0xFF) * 0.299f + (color shr 8 and 0xFF) * 0.587f + (color and 0xFF) * 0.114f)) / 255.0f
    }
}
