package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.monetization.LifeVaultBannerAd
import com.example.ui.VaultViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VaultViewModel,
    onNavigateToPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var exportedPayload by remember { mutableStateOf<String?>(null) }

    var showImportDialog by remember { mutableStateOf(false) }
    var importPayloadText by remember { mutableStateOf("") }
    var importPassword by remember { mutableStateOf("") }

    var showClearAllDialog by remember { mutableStateOf(false) }
    var showDemoSeedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Vault Privacy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            LifeVaultBannerAd()
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Privacy & Zero-Cloud Guarantee Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "100% On-Device Privacy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your receipts, serial numbers, and personal records never leave this device. No account, no cloud servers, no tracking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Backup & Encryption
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Backup & Data Export",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    SettingsActionRow(
                        title = "Export Encrypted Backup",
                        subtitle = "Save a secure AES-256-GCM encrypted backup file",
                        icon = Icons.Default.CloudUpload,
                        onClick = { showExportDialog = true },
                        tag = "export_backup_row"
                    )

                    SettingsActionRow(
                        title = "Import Backup File",
                        subtitle = "Restore records from a previously exported file",
                        icon = Icons.Default.CloudDownload,
                        onClick = { showImportDialog = true },
                        tag = "import_backup_row"
                    )
                }
            }

            // Demo & Sample Data Management
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Demo Mode & Testing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    SettingsActionRow(
                        title = "Load Sample Showcase Records",
                        subtitle = "Populates 6 realistic sample items (marked as demo)",
                        icon = Icons.Default.Science,
                        onClick = { showDemoSeedDialog = true },
                        tag = "load_sample_data_row"
                    )

                    SettingsActionRow(
                        title = "Clear Demo Records",
                        subtitle = "Removes only sample items without touching user data",
                        icon = Icons.Default.CleaningServices,
                        onClick = {
                            viewModel.clearDemoData()
                            Toast.makeText(context, "Demo records removed", Toast.LENGTH_SHORT).show()
                        },
                        tag = "clear_demo_data_row"
                    )

                    SettingsActionRow(
                        title = "Erase All App Records",
                        subtitle = "Permanently deletes all records, receipts, and alarms",
                        icon = Icons.Default.DeleteForever,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { showClearAllDialog = true },
                        tag = "erase_all_data_row"
                    )
                }
            }

            // Monetization & AdMob Policy
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Monetization & Advertising",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    SettingsActionRow(
                        title = "Privacy Policy & Data Safety",
                        subtitle = "Read our strict privacy guarantees and Play Store disclosures",
                        icon = Icons.Default.Policy,
                        onClick = onNavigateToPrivacy,
                        tag = "privacy_policy_row"
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdsClick,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google AdMob Status",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Test Ads Mode Active (Non-intrusive banners and frequency-capped interstitials)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // App Version Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LifeVault v1.0 • Built with Kotlin & Jetpack Compose",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
                exportedPayload = null
            },
            title = { Text("Export Vault Backup") },
            text = {
                Column {
                    Text("Enter an optional password to encrypt your backup with AES-256-GCM. Leave blank for unencrypted JSON.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { exportPassword = it },
                        label = { Text("Encryption Password (Optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_password_input")
                    )

                    if (exportedPayload != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Backup successfully generated! Tap below to share or copy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                if (exportedPayload == null) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = viewModel.exportBackup(exportPassword.ifBlank { null })
                                exportedPayload = result
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, result)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Save LifeVault Backup"))
                            }
                        },
                        modifier = Modifier.testTag("confirm_export_button")
                    ) {
                        Text("Export & Share")
                    }
                } else {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("LifeVault Backup", exportedPayload)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied backup payload to clipboard", Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                            exportedPayload = null
                        }
                    ) {
                        Text("Copy to Clipboard")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    exportedPayload = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Vault Backup") },
            text = {
                Column {
                    Text("Paste your exported JSON or AES encrypted backup text:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importPayloadText,
                        onValueChange = { importPayloadText = it },
                        label = { Text("Backup Payload") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("import_payload_input"),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importPassword,
                        onValueChange = { importPassword = it },
                        label = { Text("Decryption Password (if encrypted)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importPayloadText.isBlank()) {
                            Toast.makeText(context, "Please paste backup data", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            val result = viewModel.importBackup(importPayloadText, importPassword.ifBlank { null })
                            result.onSuccess { count ->
                                Toast.makeText(context, "Successfully imported $count records", Toast.LENGTH_LONG).show()
                                showImportDialog = false
                                importPayloadText = ""
                                importPassword = ""
                            }.onFailure { err ->
                                Toast.makeText(context, "Import failed: ${err.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_import_button")
                ) {
                    Text("Import Records")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Erase All Data?") },
            text = { Text("This will permanently remove all product warranties, receipts, return windows, and scheduled reminders from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearAllDialog = false
                        Toast.makeText(context, "All records wiped", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_erase_button")
                ) {
                    Text("Wipe Everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Demo Seed Dialog
    if (showDemoSeedDialog) {
        AlertDialog(
            onDismissRequest = { showDemoSeedDialog = false },
            title = { Text("Load Sample Showcase Data?") },
            text = { Text("This inserts 6 sample items (MacBook, Sony Headphones, Dyson Vacuum, Passport, Breville, Patagonia) marked as demo records. You can delete them at any time with 'Clear Demo Records'.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadDemoData()
                        showDemoSeedDialog = false
                        Toast.makeText(context, "Sample records loaded", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_seed_demo_button")
                ) {
                    Text("Load Sample Records")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoSeedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}
