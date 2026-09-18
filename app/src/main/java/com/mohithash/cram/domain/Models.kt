package com.mohithash.cram.domain

import kotlinx.serialization.Serializable

@Serializable data class Term(val term: String, val definition: String)
@Serializable data class Question(val question: String, val options: List<String>, val answer: Int, val explanation: String = "")

@Serializable
data class StudyContent(
    val title: String = "",
    val summary: List<String> = emptyList(),
    val key_points: List<String> = emptyList(),
    val terms: List<Term> = emptyList(),
    val quiz: List<Question> = emptyList(),
    val difficulty: String = "",
)

@Serializable data class Settings(val name: String = "", val level: String = "high school", val onboarded: Boolean = false)
