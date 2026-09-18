@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.cram.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mohithash.cram.ui.AppViewModel
import com.mohithash.cram.ui.Job
import com.mohithash.cram.ui.Label
import com.mohithash.cram.ui.MealPhoto
import com.mohithash.cram.ui.Photo
import com.mohithash.cram.ui.StatCard

@Composable
fun CreateScreen(vm: AppViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val ai by vm.ai.collectAsState()
    val job by vm.build.collectAsState()
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<MealPhoto?>(null) }
    val ctx = LocalContext.current
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { b -> b?.let { photo = Photo.fromBitmap(it) } }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { u -> u?.let { photo = Photo.fromUri(ctx, it) } }
    LaunchedEffect(job) { if (job is Job.Done) { vm.clearBuild(); onDone() } }

    Scaffold(topBar = { TopAppBar(title = { Text("New study set") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard {
                Label("Source material")
                photo?.let { p -> Box(Modifier.fillMaxWidth()) { Image(p.bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(220.dp).clip(MaterialTheme.shapes.large)); FilledTonalIconButton({ photo = null }, Modifier.align(Alignment.TopEnd).padding(8.dp)) { Icon(Icons.Default.Close, null) } } }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ camera.launch(null) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Snap a page") }
                    OutlinedButton({ gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Gallery") }
                }
                OutlinedTextField(text, { text = it }, placeholder = { Text("…or paste notes, an article, a chapter, lecture transcript") }, modifier = Modifier.fillMaxWidth(), minLines = 8, shape = MaterialTheme.shapes.large)
                OutlinedTextField(subject, { subject = it }, label = { Text("Subject (optional)") }, placeholder = { Text("Biology, History, Kotlin…") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
            }
            (job as? Job.Failed)?.let { StatCard(container = cs.errorContainer) { Text(it.message, color = cs.onErrorContainer) } }
            if (!ai.configured) Text("Add an API key in Settings to generate study sets.", color = cs.error, style = MaterialTheme.typography.bodySmall)
            Button({ vm.buildSet(text, photo?.base64, subject) }, enabled = ai.configured && (text.length > 20 || photo != null) && job != Job.Loading, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                if (job == Job.Loading) { LoadingIndicator(Modifier.size(22.dp)); Spacer(Modifier.size(10.dp)); Text("Building summary, cards & quiz…") } else { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text("Generate study set", style = MaterialTheme.typography.titleMedium) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
