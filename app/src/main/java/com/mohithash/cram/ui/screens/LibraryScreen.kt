@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.cram.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.cram.ui.AnimatedNumber
import com.mohithash.cram.ui.AppViewModel
import com.mohithash.cram.ui.EmptyState
import com.mohithash.cram.ui.HeroCard
import com.mohithash.cram.ui.Label
import com.mohithash.cram.ui.ShapeIcon
import com.mohithash.cram.ui.StatCard
import com.mohithash.cram.ui.theme.Brand

@Composable
fun LibraryScreen(vm: AppViewModel, onCreate: () -> Unit, onOpen: () -> Unit, onSettings: () -> Unit) {
    val sets by vm.sets.collectAsState()
    val s by vm.settings.collectAsState()
    val cs = MaterialTheme.colorScheme
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val totalTerms = sets.sumOf { vm.decode(it).terms.size }
    val known = sets.sumOf { vm.knownSet(it).size }
    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { MediumFlexibleTopAppBar(title = { Text(if (s.name.isBlank()) "Study sets" else "${s.name}'s sets") }, subtitle = { Text("${sets.size} sets · $known of $totalTerms terms known") },
            actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }, scrollBehavior = scroll, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface, scrolledContainerColor = cs.surface)) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onCreate, icon = { Icon(Icons.Default.Add, null) }, text = { Text("New set") }, containerColor = cs.primary, contentColor = cs.onPrimary) },
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie12Sided) {
                    val on = cs.onPrimary
                    Label("Mastery", on.copy(alpha = 0.8f))
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) { AnimatedNumber(known, MaterialTheme.typography.displayMedium, on); Text("/ $totalTerms terms", color = on.copy(alpha = 0.85f), modifier = Modifier.padding(bottom = 10.dp)) }
                    LinearWavyProgressIndicator(progress = { if (totalTerms == 0) 0f else known.toFloat() / totalTerms }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), color = cs.secondary, trackColor = on.copy(alpha = 0.18f))
                    Text("Paste notes or snap a textbook page → summary, flashcards and a quiz.", color = on.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (sets.isEmpty()) item { EmptyState(Icons.Default.MenuBook, "Nothing to study yet", "Tap New set to turn any material into a study set.") }
            items(sets, key = { it.id }) { set ->
                val c = vm.decode(set); val k = vm.knownSet(set).size
                StatCard(Modifier.clip(MaterialTheme.shapes.extraLarge).clickable { vm.open(set); onOpen() }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShapeIcon(Icons.Default.MenuBook, cs.secondaryContainer, cs.onSecondaryContainer, MaterialShapes.Clover4Leaf)
                        Column(Modifier.weight(1f)) {
                            Text(set.title, style = MaterialTheme.typography.titleMedium)
                            Text(listOf(set.subject, c.difficulty, "${c.terms.size} cards", "${c.quiz.size} Qs", if (set.bestScore >= 0) "best ${set.bestScore}%" else "").filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                        }
                        IconButton({ vm.delete(set.id) }) { Icon(Icons.Default.Delete, null, tint = cs.onSurfaceVariant) }
                    }
                    LinearWavyProgressIndicator(progress = { if (c.terms.isEmpty()) 0f else k.toFloat() / c.terms.size }, modifier = Modifier.fillMaxWidth())
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}
