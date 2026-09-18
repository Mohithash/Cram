@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.cram.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohithash.cram.ui.screens.CreateScreen
import com.mohithash.cram.ui.screens.LibraryScreen
import com.mohithash.cram.ui.screens.OnboardingScreen
import com.mohithash.cram.ui.screens.SetScreen
import com.mohithash.cram.ui.screens.SettingsScreen

@Composable
fun Nav(vm: AppViewModel) {
    val s by vm.settings.collectAsState()
    if (!s.onboarded) { OnboardingScreen(vm); return }
    val nav = rememberNavController()
    NavHost(nav, "library") {
        composable("library") { LibraryScreen(vm, onCreate = { nav.navigate("create") }, onOpen = { nav.navigate("set") }, onSettings = { nav.navigate("settings") }) }
        composable("create") { CreateScreen(vm, onBack = { nav.popBackStack() }, onDone = { nav.popBackStack(); nav.navigate("set") }) }
        composable("set") { SetScreen(vm, onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
    }
}
