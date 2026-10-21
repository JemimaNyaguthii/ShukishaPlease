package com.jem.shukishaplease.core.database

import android.content.Context
import com.jem.shukishaplease.core.model.TriviaQuestion
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class LocalQuestionCache(private val context: Context) {
    private val cacheFile = File(context.filesDir, "trivia_cache.json")

    fun saveCachedQuestions(category: String, questions: List<TriviaQuestion>) {
        try {
            val allCached = loadAllCached()
            val jsonArray = JSONArray()

            allCached.filter { it.category != category }.forEach { q ->
                jsonArray.put(questionToJson(q))
            }

            questions.forEach { q ->
                jsonArray.put(questionToJson(q))
            }

            cacheFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            // Ignore cache write errors
        }
    }

    fun getCachedQuestions(category: String, difficulty: String? = null): List<TriviaQuestion> {
        val all = loadAllCached()
        return all.filter { q ->
            (category.equals("All", ignoreCase = true) || q.category.equals(category, ignoreCase = true)) &&
                    (difficulty == null || difficulty.equals("All", ignoreCase = true) || q.difficulty.equals(difficulty, ignoreCase = true))
        }
    }

    private fun loadAllCached(): List<TriviaQuestion> {
        val list = mutableListOf<TriviaQuestion>()
        try {
            if (!cacheFile.exists()) return emptyList()
            val text = cacheFile.readText()
            val jsonArray = JSONArray(text)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val optionsArr = obj.getJSONArray("options")
                val optionsList = mutableListOf<String>()
                for (j in 0 until optionsArr.length()) {
                    optionsList.add(optionsArr.getString(j))
                }
                list.add(
                    TriviaQuestion(
                        id = obj.getString("id"),
                        category = obj.getString("category"),
                        questionText = obj.getString("questionText"),
                        options = optionsList,
                        correctAnswerIndex = obj.getInt("correctAnswerIndex"),
                        explanation = obj.getString("explanation"),
                        difficulty = obj.optString("difficulty", "Medium"),
                        reference = obj.optString("reference", "Cache")
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty on failure
        }
        return list
    }

    private fun questionToJson(q: TriviaQuestion): JSONObject {
        val obj = JSONObject()
        obj.put("id", q.id)
        obj.put("category", q.category)
        obj.put("questionText", q.questionText)
        val arr = JSONArray()
        q.options.forEach { arr.put(it) }
        obj.put("options", arr)
        obj.put("correctAnswerIndex", q.correctAnswerIndex)
        obj.put("explanation", q.explanation)
        obj.put("difficulty", q.difficulty)
        obj.put("reference", q.reference)
        return obj
    }
}
