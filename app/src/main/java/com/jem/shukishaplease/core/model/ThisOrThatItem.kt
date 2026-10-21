package com.jem.shukishaplease.core.model

data class ThisOrThatItem(
    val id: String,
    val prompt: String,
    val optionA: String,
    val optionB: String,
    val category: String
)
