package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VaultViewModel
import com.example.ui.screens.AddEditItemScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ItemDetailScreen
import com.example.ui.screens.ItemListScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LifeVaultTheme

sealed class NavDestination {
    object Dashboard : NavDestination()
    object ItemList : NavDestination()
    object Timeline : NavDestination()
    object Settings : NavDestination()
    data class Detail(val itemId: Long) : NavDestination()
    data class AddEdit(val itemId: Long? = null, val openCameraOnLaunch: Boolean = false) : NavDestination()
    object PrivacyPolicy : NavDestination()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LifeVaultApplication
        val extraItemId = intent.getLongExtra("EXTRA_ITEM_ID", -1L)

        setContent {
            LifeVaultTheme {
                val viewModel: VaultViewModel = viewModel {
                    VaultViewModel(app, app.repository)
                }

                LifeVaultAppRoot(
                    viewModel = viewModel,
                    initialItemId = if (extraItemId > 0L) extraItemId else null
                )
            }
        }
    }
}

@Composable
fun LifeVaultAppRoot(
    viewModel: VaultViewModel,
    initialItemId: Long? = null
) {
    val context = LocalContext.current
    val navStack = remember {
        mutableStateListOf<NavDestination>(
            initialItemId?.let { NavDestination.Detail(it) } ?: NavDestination.Dashboard
        )
    }

    val currentDestination = navStack.lastOrNull() ?: NavDestination.Dashboard

    // Android 13+ POST_NOTIFICATIONS permission request
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Graceful callback; alarms still schedule and notifications show once granted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // BackHandler to handle subscreen popping
    BackHandler(enabled = navStack.size > 1) {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.size - 1)
        }
    }

    val isTopLevelScreen = currentDestination in listOf(
        NavDestination.Dashboard,
        NavDestination.ItemList,
        NavDestination.Timeline,
        NavDestination.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentDestination is NavDestination.Dashboard,
                        onClick = {
                            navStack.clear()
                            navStack.add(NavDestination.Dashboard)
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentDestination is NavDestination.ItemList,
                        onClick = {
                            navStack.clear()
                            navStack.add(NavDestination.ItemList)
                        },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Vault") },
                        label = { Text("Vault") },
                        modifier = Modifier.testTag("nav_tab_vault")
                    )

                    NavigationBarItem(
                        selected = currentDestination is NavDestination.Timeline,
                        onClick = {
                            navStack.clear()
                            navStack.add(NavDestination.Timeline)
                        },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Timeline") },
                        label = { Text("Timeline") },
                        modifier = Modifier.testTag("nav_tab_timeline")
                    )

                    NavigationBarItem(
                        selected = currentDestination is NavDestination.Settings,
                        onClick = {
                            navStack.clear()
                            navStack.add(NavDestination.Settings)
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            is NavDestination.Dashboard -> {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = { navStack.add(NavDestination.AddEdit(null, openCameraOnLaunch = false)) },
                    onNavigateToScan = { navStack.add(NavDestination.AddEdit(null, openCameraOnLaunch = true)) },
                    onNavigateToList = {
                        navStack.clear()
                        navStack.add(NavDestination.ItemList)
                    },
                    onNavigateToItem = { id -> navStack.add(NavDestination.Detail(id)) },
                    onNavigateToTimeline = {
                        navStack.clear()
                        navStack.add(NavDestination.Timeline)
                    },
                    onNavigateToSettings = {
                        navStack.clear()
                        navStack.add(NavDestination.Settings)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is NavDestination.ItemList -> {
                ItemListScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = { navStack.add(NavDestination.AddEdit(null)) },
                    onNavigateToItem = { id -> navStack.add(NavDestination.Detail(id)) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is NavDestination.Timeline -> {
                TimelineScreen(
                    viewModel = viewModel,
                    onNavigateToItem = { id -> navStack.add(NavDestination.Detail(id)) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is NavDestination.Settings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToPrivacy = { navStack.add(NavDestination.PrivacyPolicy) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is NavDestination.Detail -> {
                ItemDetailScreen(
                    itemId = currentDestination.itemId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (navStack.size > 1) navStack.removeAt(navStack.size - 1)
                        else {
                            navStack.clear()
                            navStack.add(NavDestination.Dashboard)
                        }
                    },
                    onNavigateToEdit = { id -> navStack.add(NavDestination.AddEdit(id)) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is NavDestination.AddEdit -> {
                AddEditItemScreen(
                    itemId = currentDestination.itemId,
                    openCameraOnLaunch = currentDestination.openCameraOnLaunch,
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (navStack.size > 1) navStack.removeAt(navStack.size - 1)
                        else {
                            navStack.clear()
                            navStack.add(NavDestination.Dashboard)
                        }
                    },
                    onSaved = { savedId ->
                        // Pop AddEdit, navigate to the saved item Detail
                        if (navStack.size > 1) navStack.removeAt(navStack.size - 1)
                        navStack.add(NavDestination.Detail(savedId))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is NavDestination.PrivacyPolicy -> {
                PrivacyPolicyScreen(
                    onNavigateBack = {
                        if (navStack.size > 1) navStack.removeAt(navStack.size - 1)
                        else {
                            navStack.clear()
                            navStack.add(NavDestination.Settings)
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
