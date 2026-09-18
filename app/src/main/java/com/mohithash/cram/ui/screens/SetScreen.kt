@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.cram.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohithash.cram.data.StudySet
import com.mohithash.cram.domain.StudyContent
import com.mohithash.cram.ui.AppViewModel
import com.mohithash.cram.ui.HeroCard
import com.mohithash.cram.ui.Job
import com.mohithash.cram.ui.Label
import com.mohithash.cram.ui.StatCard
import com.mohithash.cram.ui.theme.Brand

private enum class Mode { SUMMARY, CARDS, QUIZ, ASK }

@Composable
fun SetScreen(vm: AppViewModel, onBack: () -> Unit) {
    val sel by vm.selected.collectAsState()
    val set = sel ?: run { onBack(); return }
    val c = vm.decode(set)
    val cs = MaterialTheme.colorScheme
    var mode by remember { mutableStateOf(Mode.SUMMARY) }
    Scaffold(topBar = { TopAppBar(title = { Text(set.title, maxLines = 1) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                Mode.entries.forEachIndexed { i, m ->
                    ToggleButton(checked = mode == m, onCheckedChange = { mode = m }, modifier = Modifier.weight(1f),
                        shapes = when (i) { 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes(); Mode.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes(); else -> ButtonGroupDefaults.connectedMiddleButtonShapes() }) {
                        Text(m.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            AnimatedContent(mode, label = "mode") { m ->
                when (m) {
                    Mode.SUMMARY -> Summary(c)
                    Mode.CARDS -> Cards(vm, set, c)
                    Mode.QUIZ -> Quiz(vm, set, c)
                    Mode.ASK -> Ask(vm, set)
                }
            }
        }
    }
}

@Composable
private fun Summary(c: StudyContent) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HeroCard(colors = listOf(cs.primary, Brand.heroDeep)) { Label("In short", cs.onPrimary.copy(alpha = 0.8f)); c.summary.forEach { Text("•  $it", color = cs.onPrimary, style = MaterialTheme.typography.bodyLarge) } }
        StatCard(container = cs.secondaryContainer) { Label("Memorise", cs.onSecondaryContainer); c.key_points.forEach { Text("★  $it", color = cs.onSecondaryContainer, style = MaterialTheme.typography.bodyLarge) } }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Cards(vm: AppViewModel, set: StudySet, c: StudyContent) {
    val cs = MaterialTheme.colorScheme
    val known = vm.knownSet(set)
    var i by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    val rot by animateFloatAsState(if (flipped) 180f else 0f, label = "flip")
    if (c.terms.isEmpty()) { Text("No cards."); return }
    val t = c.terms[i % c.terms.size]
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LinearWavyProgressIndicator(progress = { known.size.toFloat() / c.terms.size }, modifier = Modifier.fillMaxWidth())
        Text("Card ${i % c.terms.size + 1} of ${c.terms.size} · ${known.size} known", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
        Card(Modifier.fillMaxWidth().weight(1f).graphicsLayer { rotationY = rot; cameraDistance = 12f * density }.clickable { flipped = !flipped }, shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = if (rot <= 90f) cs.primaryContainer else cs.tertiaryContainer)) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                if (rot <= 90f) Text(t.term, style = MaterialTheme.typography.headlineMedium, color = cs.onPrimaryContainer, textAlign = TextAlign.Center)
                else Text(t.definition, style = MaterialTheme.typography.titleLarge, color = cs.onTertiaryContainer, textAlign = TextAlign.Center, modifier = Modifier.graphicsLayer { rotationY = 180f })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton({ i++; flipped = false }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = cs.errorContainer)) { Icon(Icons.Default.Close, null); Text(" Still learning") }
            Button({ if (i % c.terms.size !in known) vm.toggleKnown(set, i % c.terms.size); i++; flipped = false }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).height(52.dp)) { Icon(Icons.Default.Check, null); Text(" Know it") }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Quiz(vm: AppViewModel, set: StudySet, c: StudyContent) {
    val cs = MaterialTheme.colorScheme
    var i by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    if (c.quiz.isEmpty()) { Text("No quiz."); return }
    if (finished) {
        val pct = score * 100 / c.quiz.size
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            HeroCard(colors = listOf(cs.primary, Brand.heroDeep)) { Label("Result", cs.onPrimary.copy(alpha = 0.8f)); Text("$pct%", style = MaterialTheme.typography.displayLarge, color = cs.onPrimary); Text("$score of ${c.quiz.size} correct" + if (pct >= set.bestScore && set.bestScore >= 0) " · new best" else "", color = cs.onPrimary.copy(alpha = 0.85f)) }
            Spacer(Modifier.height(16.dp))
            Button({ i = 0; picked = null; score = 0; finished = false }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Try again") }
        }
        return
    }
    val q = c.quiz[i]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LinearWavyProgressIndicator(progress = { i.toFloat() / c.quiz.size }, modifier = Modifier.fillMaxWidth())
        Text("Question ${i + 1} of ${c.quiz.size} · $score correct", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
        Text(q.question, style = MaterialTheme.typography.titleLarge)
        q.options.forEachIndexed { oi, opt ->
            val state = when { picked == null -> cs.surfaceContainerLow; oi == q.answer -> cs.primaryContainer; oi == picked -> cs.errorContainer; else -> cs.surfaceContainerLow }
            StatCard(Modifier.clickable(enabled = picked == null) { picked = oi; if (oi == q.answer) score++ }, container = state) { Text(opt, style = MaterialTheme.typography.bodyLarge) }
        }
        if (picked != null) {
            Text(if (picked == q.answer) "✅ Correct. ${q.explanation}" else "❌ ${q.explanation}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            Button({ if (i + 1 >= c.quiz.size) { finished = true; vm.recordScore(set, score * 100 / c.quiz.size) } else { i++; picked = null } }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(if (i + 1 >= c.quiz.size) "See result" else "Next") }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Ask(vm: AppViewModel, set: StudySet) {
    val cs = MaterialTheme.colorScheme
    val ans by vm.ask.collectAsState()
    val ai by vm.ai.collectAsState()
    var q by remember { mutableStateOf("") }
    var asked by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ask anything about this set — “explain X like I'm 12”, “why does Y happen?”, “give me a mnemonic for Z”.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(q, { q = it }, placeholder = { Text("Your question") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.extraLarge, maxLines = 3)
            FilledIconButton({ asked = q; vm.askTutor(set, q); q = "" }, enabled = q.isNotBlank() && ai.configured && ans != Job.Loading, modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Filled.Send, null) }
        }
        if (asked.isNotBlank()) StatCard(container = cs.secondaryContainer) { Text(asked, color = cs.onSecondaryContainer, style = MaterialTheme.typography.titleMedium) }
        when (val a = ans) {
            Job.Loading -> Row(verticalAlignment = Alignment.CenterVertically) { LoadingIndicator(); Spacer(Modifier.size(10.dp)); Text("Thinking…", color = cs.onSurfaceVariant) }
            is Job.Done -> StatCard { Text(a.value, style = MaterialTheme.typography.bodyLarge) }
            is Job.Failed -> Text(a.message, color = cs.error)
            Job.Idle -> {}
        }
    }
}
