package com.jem.shukishaplease.features.trivia

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TriviaViewModel : ViewModel() {
    private val repository = TriviaRepository()

    private val _uiState = MutableStateFlow(TriviaUiState())
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()

    val categories: List<String> = repository.categories

    fun loadQuestions(context: Context? = null) {
        val repo = context?.let { TriviaRepository(it) } ?: repository
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val (questions, aiError) = repo.getQuestions(
                category = _uiState.value.selectedCategory,
                difficulty = _uiState.value.selectedDifficulty
            )
            val finalQuestions = questions.ifEmpty {
                repo.getQuestions("All", "All").first
            }.shuffled()

            _uiState.update {
                it.copy(
                    questions = finalQuestions,
                    currentIndex = 0,
                    selectedAnswerIndex = null,
                    isAnswerSubmitted = false,
                    score = 0,
                    isFinished = false,
                    isLoading = false,
                    errorMessage = aiError
                )
            }
        }
    }

    fun setCategory(context: Context? = null, category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadQuestions(context)
    }

    fun setDifficulty(context: Context? = null, difficulty: String) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
        loadQuestions(context)
    }

    fun selectAnswer(index: Int) {
        if (!_uiState.value.isAnswerSubmitted) {
            _uiState.update { it.copy(selectedAnswerIndex = index) }
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        if (state.selectedAnswerIndex == null || state.isAnswerSubmitted) return

        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
        val isCorrect = state.selectedAnswerIndex == currentQ.correctAnswerIndex

        _uiState.update {
            it.copy(
                isAnswerSubmitted = true,
                score = if (isCorrect) it.score + 1 else it.score
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        val nextIdx = state.currentIndex + 1
        if (nextIdx < state.questions.size) {
            _uiState.update {
                it.copy(
                    currentIndex = nextIdx,
                    selectedAnswerIndex = null,
                    isAnswerSubmitted = false
                )
            }
        } else {
            _uiState.update { it.copy(isFinished = true) }
        }
    }
}
