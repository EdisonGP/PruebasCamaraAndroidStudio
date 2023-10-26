package com.ingenieriajhr.testopencv

import kotlin.math.pow
import kotlin.math.sqrt
/*
class Result(
    private var predictedEmbedding: FloatArray,
    private var groundTruthEmbeddings: Map<String, FloatArray>,
    private var threshold: Float
) {
    var mPersonClass = findClosestEmbedding(predictedEmbedding)

    private fun findClosestEmbedding(predictedEmbedding: FloatArray): String? {
        var minDistance = Double.MAX_VALUE
        var closestClass: String? = null

        for ((label, embedding) in groundTruthEmbeddings) {
            val distance = euclideanDistance(predictedEmbedding, embedding)
            if (distance < minDistance) {
                minDistance = distance.toDouble()
                closestClass = label
            }
        }

        return if (minDistance < threshold) closestClass else "Unknown"
    }

    private fun euclideanDistance(a: FloatArray, b: FloatArray): Float {
        return sqrt(a.zip(b).map { (x, y) -> (x - y).pow(2) }.sum())
    }
}*/

class Result(
    private var probs: FloatArray,
    private var timeCost: Long,
    private var names: List<String> = listOf("Alice Yepez", "Antonio Guzman", "Byron Ford", "Carolina", "Cynthia", "Danilo", "David", "Diana", "Emilia", "Linda Chavez", "Lourdes", "Nadia", "Susana", "Veronica", "Edison", "Maria")
) {

    var mNumber = argmax(probs)
    var mProbability = probs[mNumber]
    var mTimeCost = timeCost
    var mPersonClass = names[mNumber]

    private fun argmax(probs: FloatArray): Int {
        var maxIdx = -1
        var maxProb = 0.0f
        for(i in probs.indices) {
            if(probs[i] > maxProb){
                maxProb = probs[i]
                maxIdx = i
            }
        }

        return maxIdx
    }

}