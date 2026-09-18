package com.mohithash.cram.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.cram.App
import com.mohithash.cram.ai.AiSettings
import com.mohithash.cram.data.StudySet
import com.mohithash.cram.domain.Settings
import com.mohithash.cram.domain.StudyContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed interface Job<out T> {
    data object Idle : Job<Nothing>
    data object Loading : Job<Nothing>
    data class Done<T>(val value: T) : Job<T>
    data class Failed(val message: String) : Job<Nothing>
}

class AppViewModel(private val app: App) : ViewModel() {
    private val db = app.db
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val client get() = app.client

    val ai: StateFlow<AiSettings> = app.store.flow("ai", AiSettings.serializer(), AiSettings())
    val settings: StateFlow<Settings> = app.store.flow("settings", Settings.serializer(), Settings())
    fun saveAi(a: AiSettings) = app.store.set("ai", AiSettings.serializer(), a)
    fun saveSettings(s: Settings) = app.store.set("settings", Settings.serializer(), s.copy(onboarded = true))

    val sets = db.sets().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val selected = MutableStateFlow<StudySet?>(null)

    private val _build = MutableStateFlow<Job<StudySet>>(Job.Idle)
    val build: StateFlow<Job<StudySet>> = _build
    private val _ask = MutableStateFlow<Job<String>>(Job.Idle)
    val ask: StateFlow<Job<String>> = _ask

    fun decode(s: StudySet): StudyContent = runCatching { json.decodeFromString(StudyContent.serializer(), s.json) }.getOrDefault(StudyContent())
    fun knownSet(s: StudySet): Set<Int> = s.known.split(',').mapNotNull { it.toIntOrNull() }.toSet()

    fun buildSet(text: String, image: String?, subject: String) {
        _build.value = Job.Loading
        viewModelScope.launch {
            _build.value = runCatching { app.study.build(ai.value, settings.value, text, image, subject) }.fold({ c ->
                val row = StudySet(title = c.title.ifBlank { "Untitled" }, subject = subject, source = if (image != null) "(photo)" else text.take(2000), json = json.encodeToString(StudyContent.serializer(), c))
                val id = db.sets().insert(row)
                val saved = row.copy(id = id); selected.value = saved; Job.Done(saved)
            }, { Job.Failed(it.message ?: "Failed") })
        }
    }
    fun clearBuild() { _build.value = Job.Idle }
    fun open(s: StudySet) { selected.value = s; _ask.value = Job.Idle }
    fun delete(id: Long) = viewModelScope.launch { db.sets().delete(id); if (selected.value?.id == id) selected.value = null }

    fun toggleKnown(s: StudySet, idx: Int) = viewModelScope.launch {
        val k = knownSet(s).toMutableSet(); if (!k.add(idx)) k.remove(idx)
        val u = s.copy(known = k.joinToString(","), lastStudied = System.currentTimeMillis()); db.sets().update(u); selected.value = u
    }
    fun recordScore(s: StudySet, score: Int) = viewModelScope.launch {
        val u = s.copy(bestScore = maxOf(s.bestScore, score), lastStudied = System.currentTimeMillis()); db.sets().update(u); selected.value = u
    }
    fun askTutor(s: StudySet, q: String) {
        _ask.value = Job.Loading
        viewModelScope.launch { _ask.value = runCatching { app.study.ask(ai.value, settings.value, decode(s), q) }.fold({ Job.Done(it) }, { Job.Failed(it.message ?: "Failed") }) }
    }
}
