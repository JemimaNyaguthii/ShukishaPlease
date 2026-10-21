package com.jem.shukishaplease.features.trivia

import com.jem.shukishaplease.core.model.TriviaQuestion

data class TriviaUiState(
    val questions: List<TriviaQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val score: Int = 0,
    val isFinished: Boolean = false,
    val selectedCategory: String = "All",
    val selectedDifficulty: String = "All",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
