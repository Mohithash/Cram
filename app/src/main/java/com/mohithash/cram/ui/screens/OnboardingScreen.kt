@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.cram.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.cram.domain.Settings
import com.mohithash.cram.ui.AppViewModel
import com.mohithash.cram.ui.Label
import com.mohithash.cram.ui.StatCard

@Composable
fun SettingsForm(initial: Settings, onChange: (Settings) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var level by remember { mutableStateOf(initial.level) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(name, { name = it; onChange(initial.copy(name = it, level = level)) }, label = { Text("Name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
        Label("I'm studying at")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("middle school", "high school", "university", "professional exam", "just curious").forEach { l -> FilterChip(selected = level == l, onClick = { level = l; onChange(initial.copy(name = name, level = l)) }, label = { Text(l) }) } }
    }
}

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    var draft by remember { mutableStateOf(Settings()) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("Study smarter") }, subtitle = { Text("Any notes, page or article → summary, flashcards and a quiz. Then ask the tutor anything.") }, scrollBehavior = scroll) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard { SettingsForm(Settings()) { draft = it } }
            Button({ vm.saveSettings(draft) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Start", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
