package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy & Data Safety") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "LifeVault Architecture: 100% Local Device Storage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            PolicySection(
                title = "1. Summary of Principles",
                body = "LifeVault (\"the App\") is engineered strictly as an offline, device-local life organizer. We believe your purchase receipts, warranty terms, return deadlines, document expiration dates, and personal property serial numbers belong solely to you. The App operates without a cloud backend, does not require user accounts or logins, and never uploads your personal records to any remote server."
            )

            PolicySection(
                title = "2. Data Handled by the App",
                body = "• Personal Records: Information you enter into LifeVault (product titles, merchant names, purchase prices, dates, serial numbers, and notes) is stored in a private SQLite database residing entirely on your physical device.\n\n• Receipt Photos: Receipt images captured or imported via the zero-permission Android Photo Picker are saved into your device's sandboxed internal app storage. They are never uploaded or shared.\n\n• On-Device ML OCR: Receipt text recognition is executed directly on your device utilizing Google ML Kit's on-device Latin models. The extracted image bytes and text never leave your phone's memory."
            )

            PolicySection(
                title = "3. Third-Party Advertising (Google AdMob)",
                body = "LifeVault is supported by non-intrusive advertising delivered via Google AdMob (Google Mobile Ads SDK).\n\n• What AdMob May Collect: In accordance with standard Google Play Services, the Google Mobile Ads SDK may automatically process device identifiers (such as the Google Advertising ID), coarse location or IP address for fraud detection, diagnostic error metrics, and ad interaction telemetry. None of your LifeVault records, receipt contents, or notes are accessible to or shared with AdMob.\n\n• Consent & Opt-Out: Users in applicable jurisdictions can manage personalized advertising preferences via Android System Settings (Settings > Google > Ads)."
            )

            PolicySection(
                title = "4. Data Backup & User Ownership",
                body = "LifeVault allows you to export your database into an encrypted JSON file secured by industry-standard AES-256-GCM encryption with PBKDF2 key derivation. The resulting file remains strictly under your control. The App never automatically synchronizes, uploads, or transmits backup files."
            )

            PolicySection(
                title = "5. Data Deletion",
                body = "You have total control over your data. You may delete individual records at any time, or use the 'Erase All App Records' action in Settings to wipe all database entries, receipt photos, and scheduled notification alarms simultaneously. Uninstalling the application removes all application data completely."
            )

            PolicySection(
                title = "6. Contact",
                body = "For inquiries regarding this privacy disclosure or application architecture, contact our developer support team at support@lifevault.local."
            )
        }
    }
}

@Composable
fun PolicySection(title: String, body: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}
