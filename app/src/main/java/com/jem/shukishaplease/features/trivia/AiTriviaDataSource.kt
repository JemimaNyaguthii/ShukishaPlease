package com.jem.shukishaplease.features.trivia

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.jem.shukishaplease.BuildConfig
import com.jem.shukishaplease.core.model.TriviaQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.UUID

class AiTriviaDataSource(private val context: Context? = null) {

    suspend fun generateAiQuestions(category: String, difficulty: String): List<TriviaQuestion> = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        Log.d("ShukishaTrivia", "AI: Request starting for category='$category', difficulty='$difficulty'")

        if (key.isBlank() || key == "YOUR_API_KEY_HERE") {
            Log.w("ShukishaTrivia", "AI: No valid API key configured in BuildConfig.")
            return@withContext fallbackToOffline(category, difficulty)
        }

        // Using supported non-retired Gemini 2.x models
        val candidateModels = listOf("gemini-2.0-flash", "gemini-2.5-flash", "gemini-2.0-flash-lite")

        for (modelName in candidateModels) {
            try {
                Log.d("ShukishaTrivia", "AI: Querying Gemini API with model '$modelName'...")
                val generativeModel = GenerativeModel(
                    modelName = modelName,
                    apiKey = key
                )

                val prompt = """
                    Generate exactly 3 multiple-choice trivia questions for category "$category" with difficulty "$difficulty".
                    Return ONLY a valid JSON array of objects. Each object must have these exact keys:
                    - "id": string (unique identifier)
                    - "questionText": string (clear question)
                    - "options": array of exactly 4 strings
                    - "correctAnswerIndex": integer between 0 and 3 (inclusive)
                    - "explanation": string (brief explanation)
                    - "reference": string (source reference)
                    Do not include any markdown formatting like ```json or ```, return only the raw JSON array.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val text = response.text?.trim() ?: continue

                Log.d("ShukishaTrivia", "AI: Response received from model '$modelName'.")

                val cleanedText = text
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val jsonArray = JSONArray(cleanedText)
                val questions = mutableListOf<TriviaQuestion>()
                val seenIds = mutableSetOf<String>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)

                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val questionText = obj.optString("questionText", "").trim()
                    val explanation = obj.optString("explanation", "").trim()
                    val reference = obj.optString("reference", "Gemini AI").trim()
                    val correctAnswerIndex = obj.optInt("correctAnswerIndex", -1)

                    val optionsArray = obj.optJSONArray("options")
                    val optionsList = mutableListOf<String>()
                    if (optionsArray != null) {
                        for (j in 0 until optionsArray.length()) {
                            optionsList.add(optionsArray.optString(j, "").trim())
                        }
                    }

                    // Strict validation rules
                    if (questionText.isBlank()) continue
                    if (optionsList.size != 4 || optionsList.any { it.isBlank() }) continue
                    if (correctAnswerIndex !in 0..3) continue
                    if (explanation.isBlank()) continue
                    if (id in seenIds) continue

                    seenIds.add(id)
                    questions.add(
                        TriviaQuestion(
                            id = id,
                            category = category,
                            questionText = questionText,
                            options = optionsList,
                            correctAnswerIndex = correctAnswerIndex,
                            explanation = explanation,
                            difficulty = difficulty,
                            reference = reference,
                            isAiGenerated = true
                        )
                    )
                }

                if (questions.size >= 3) {
                    Log.d("ShukishaTrivia", "AI: Successfully generated ${questions.size} valid questions via '$modelName'.")
                    return@withContext questions.take(3)
                }
            } catch (e: Exception) {
                // Log diagnostic error safely without printing keys or credentials
                Log.e("ShukishaTrivia", "AI: Model '$modelName' failed: ${e::class.java.simpleName} - ${e.localizedMessage}")
            }
        }

        Log.w("ShukishaTrivia", "AI: Generation failed or empty for all candidate models. Falling back to offline bank.")
        fallbackToOffline(category, difficulty)
    }

    private fun fallbackToOffline(category: String, difficulty: String): List<TriviaQuestion> {
        if (context != null) {
            val assetSource = AssetTriviaDataSource(context)
            return assetSource.getQuestionsForCategory(category, difficulty)
        }
        return emptyList()
    }
}
