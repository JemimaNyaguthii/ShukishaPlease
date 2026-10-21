package com.jem.shukishaplease.core.model

data class WordItem(
    val id: String,
    val word: String,
    val hint: String,
    val category: String,
    val difficulty: String = "Easy"
)
