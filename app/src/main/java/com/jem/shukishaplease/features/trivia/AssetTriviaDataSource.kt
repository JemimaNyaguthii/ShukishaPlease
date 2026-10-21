package com.jem.shukishaplease.features.trivia

import android.content.Context
import com.jem.shukishaplease.core.model.TriviaQuestion
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class AssetTriviaDataSource(private val context: Context) {

    fun getQuestionsForCategory(category: String, difficulty: String): List<TriviaQuestion> {
        return try {
            val inputStream = context.assets.open("trivia_bank.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            val questions = mutableListOf<TriviaQuestion>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val cat = obj.getString("category")

                if (category.equals("All", ignoreCase = true) || cat.equals(category, ignoreCase = true)) {
                    val optionsArray = obj.getJSONArray("options")
                    val optionsList = mutableListOf<String>()
                    for (j in 0 until optionsArray.length()) {
                        optionsList.add(optionsArray.getString(j))
                    }

                    questions.add(
                        TriviaQuestion(
                            id = obj.getString("id"),
                            category = cat,
                            questionText = obj.getString("questionText"),
                            options = optionsList,
                            correctAnswerIndex = obj.getInt("correctAnswerIndex"),
                            explanation = obj.getString("explanation"),
                            difficulty = obj.optString("difficulty", difficulty),
                            reference = obj.optString("reference", ""),
                            isAiGenerated = true
                        )
                    )
                }
            }
            questions.shuffled().take(3)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
