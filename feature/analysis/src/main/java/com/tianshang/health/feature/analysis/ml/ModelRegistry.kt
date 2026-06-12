package com.tianshang.health.feature.analysis.ml

data class ModelSpec(
    val name: String,
    val assetPath: String,
    val version: Int,
    val inputShape: IntArray,
    val outputShape: IntArray,
    val inputDataType: Class<*>,
    val outputDataType: Class<*>,
    val mean: FloatArray,
    val std: FloatArray,
    val outputMean: Float = 29.3f,
    val outputStd: Float = 3.9f
)

object ModelRegistry {

    const val PERIOD_LSTM = "period_lstm"
    const val PERIOD_LINEAR = "period_linear"
    const val MOOD_MLP = "mood_mlp"

    const val MIN_CYCLES_FOR_LSTM = 6
    const val MIN_CYCLES_FOR_LINEAR = 3
    const val MIN_CYCLES_FOR_PREDICTION = 3

    const val CONFIDENCE_THRESHOLD = 0.3f

    private const val MODELS_DIR = "models"

    private val LINEAR_MEAN = floatArrayOf(29.3f, 13.3f, 5.2f, 29.3f, 24.2f, 30.5f)
    private val LINEAR_STD = floatArrayOf(3.9f, 2.6f, 1.3f, 2.8f, 1.6f, 1.7f)

    val availableModels: Map<String, ModelSpec> = mapOf(
        PERIOD_LSTM to ModelSpec(
            name = PERIOD_LSTM,
            assetPath = "$MODELS_DIR/period_lstm_v2.tflite",
            version = 2,
            inputShape = intArrayOf(1, 12, 6),
            outputShape = intArrayOf(1, 2),
            inputDataType = FloatArray::class.java,
            outputDataType = FloatArray::class.java,
            mean = LINEAR_MEAN,
            std = LINEAR_STD,
            outputMean = 29.3f,
            outputStd = 3.9f
        ),
        PERIOD_LINEAR to ModelSpec(
            name = PERIOD_LINEAR,
            assetPath = "$MODELS_DIR/period_linear_v2.tflite",
            version = 2,
            inputShape = intArrayOf(1, 6),
            outputShape = intArrayOf(1, 2),
            inputDataType = FloatArray::class.java,
            outputDataType = FloatArray::class.java,
            mean = LINEAR_MEAN,
            std = LINEAR_STD,
            outputMean = 29.3f,
            outputStd = 3.9f
        ),
        MOOD_MLP to ModelSpec(
            name = MOOD_MLP,
            assetPath = "$MODELS_DIR/mood_mlp_v1.tflite",
            version = 1,
            inputShape = intArrayOf(1, 15),
            outputShape = intArrayOf(1, 2),
            inputDataType = FloatArray::class.java,
            outputDataType = FloatArray::class.java,
            mean = floatArrayOf(),
            std = floatArrayOf(),
            outputMean = 0f,
            outputStd = 0f
        )
    )

    fun getModelSpec(modelName: String): ModelSpec? = availableModels[modelName]
}
