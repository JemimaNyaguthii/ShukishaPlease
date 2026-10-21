package com.jem.shukishaplease.core.model

data class TriviaQuestion(
    val id: String,
    val category: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String = "Medium",
    val reference: String = "",
    val isAiGenerated: Boolean = false
)
