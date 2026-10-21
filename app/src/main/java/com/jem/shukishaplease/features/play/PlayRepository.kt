package com.jem.shukishaplease.features.play

import android.content.Context
import com.jem.shukishaplease.core.model.ThisOrThatItem
import com.jem.shukishaplease.core.model.WordItem
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class PlayRepository(private val context: Context) {

    fun getWordItems(): List<WordItem> {
        return try {
            val inputStream = context.assets.open("word_bank.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            val words = mutableListOf<WordItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                words.add(
                    WordItem(
                        id = obj.getString("id"),
                        word = obj.getString("word").uppercase(),
                        hint = obj.getString("hint"),
                        category = obj.getString("category"),
                        difficulty = obj.optString("difficulty", "Easy")
                    )
                )
            }
            words
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getThisOrThatItems(): List<ThisOrThatItem> {
        return try {
            val inputStream = context.assets.open("this_or_that.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            val items = mutableListOf<ThisOrThatItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    ThisOrThatItem(
                        id = obj.getString("id"),
                        prompt = obj.getString("prompt"),
                        optionA = obj.getString("optionA"),
                        optionB = obj.getString("optionB"),
                        category = obj.getString("category")
                    )
                )
            }
            items
        } catch (e: Exception) {
            emptyList()
        }
    }
}
