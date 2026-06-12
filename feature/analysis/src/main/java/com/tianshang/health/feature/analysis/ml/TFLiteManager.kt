package com.tianshang.health.feature.analysis.ml

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TFLiteManager @Inject constructor(
    private val context: Context
) {
    private val interpreters = mutableMapOf<String, CloseableInterpreter>()
    private val cacheDir = File(context.filesDir, CACHE_DIR).also { it.mkdirs() }

    fun runInference(
        modelName: String,
        input: FloatArray,
        spec: ModelSpec
    ): FloatArray? {
        return try {
            val interpreter = getInterpreter(modelName, spec) ?: return null
            val inputBuffer = convertToByteBuffer(input)
            val outputBuffer = ByteBuffer.allocateDirect(
                spec.outputShape.reduce { a, b -> a * b } * 4
            ).order(ByteOrder.nativeOrder())

            interpreter.interpret.run(inputBuffer, outputBuffer)

            val result = FloatArray(spec.outputShape.reduce { a, b -> a * b })
            outputBuffer.rewind()
            outputBuffer.asFloatBuffer().get(result)
            result
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.w(TAG, "TFLite inference failed for $modelName", e)
            null
        }
    }

    private fun getInterpreter(modelName: String, spec: ModelSpec): CloseableInterpreter? {
        interpreters[modelName]?.let { return it }

        val modelFile = getModelFile(modelName, spec) ?: return null

        return try {
            val interpreter = org.tensorflow.lite.Interpreter(modelFile)
            val ci = CloseableInterpreter(interpreter)
            interpreters[modelName] = ci
            ci
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.e(TAG, "Failed to create interpreter for $modelName", e)
            null
        }
    }

    private fun getModelFile(modelName: String, spec: ModelSpec): MappedByteBuffer? {
        val cachedFile = File(cacheDir, "${modelName}_v${spec.version}.tflite")
        if (cachedFile.exists()) {
            return try {
                FileChannel.open(cachedFile.toPath(), StandardOpenOption.READ)
                    .map(FileChannel.MapMode.READ_ONLY, 0, cachedFile.length())
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.w(TAG, "Failed to load cached model $modelName", e)
                cachedFile.delete()
                null
            }
        }

        return try {
            context.assets.open(spec.assetPath).use { input ->
                FileOutputStream(cachedFile).use { output ->
                    input.copyTo(output)
                }
            }
            FileChannel.open(cachedFile.toPath(), StandardOpenOption.READ)
                .map(FileChannel.MapMode.READ_ONLY, 0, cachedFile.length())
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.e(TAG, "Model file not found: ${spec.assetPath}", e)
            null
        }
    }

    fun isModelAvailable(modelName: String): Boolean {
        val spec = ModelRegistry.getModelSpec(modelName) ?: return false
        return try {
            context.assets.list("models")?.contains(spec.assetPath.removePrefix("models/")) == true ||
                File(cacheDir, "${modelName}_v${spec.version}.tflite").exists()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            false
        }
    }

    fun warmUp(modelName: String, spec: ModelSpec) {
        getInterpreter(modelName, spec)
    }

    fun close(modelName: String) {
        interpreters.remove(modelName)?.interpret?.close()
    }

    fun closeAll() {
        interpreters.values.forEach { it.interpret.close() }
        interpreters.clear()
    }

    private fun convertToByteBuffer(input: FloatArray): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(input.size * 4).order(ByteOrder.nativeOrder())
        input.forEach { buffer.putFloat(it) }
        buffer.rewind()
        return buffer
    }

    data class CloseableInterpreter(val interpret: org.tensorflow.lite.Interpreter)

    companion object {
        private const val TAG = "TFLiteManager"
        private const val CACHE_DIR = "tflite_models"
    }
}
