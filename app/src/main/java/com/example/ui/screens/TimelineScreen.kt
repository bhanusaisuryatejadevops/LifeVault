package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VaultItem
import com.example.monetization.LifeVaultBannerAd
import com.example.ui.VaultViewModel
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusExpiredRed
import com.example.ui.theme.StatusExpiringAmber
import com.example.ui.theme.StatusNeutralBlue
import java.util.concurrent.TimeUnit

data class TimelineEvent(
    val item: VaultItem,
    val eventType: String,
    val targetDate: Long,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: VaultViewModel,
    onNavigateToItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.activeItems.collectAsStateWithLifecycle()
    val now = System.currentTimeMillis()

    // Flatten all events across items
    val allEvents = mutableListOf<TimelineEvent>()
    for (item in items) {
        item.returnDeadlineDate?.let {
            allEvents.add(
                TimelineEvent(
                    item = item,
                    eventType = "Return Deadline",
                    targetDate = it,
                    icon = Icons.Default.ShoppingBag,
                    color = StatusExpiringAmber
                )
            )
        }
        item.warrantyEndDate?.let {
            allEvents.add(
                TimelineEvent(
                    item = item,
                    eventType = "Warranty Expiration",
                    targetDate = it,
                    icon = Icons.Default.Security,
                    color = if (it < now) StatusExpiredRed else StatusNeutralBlue
                )
            )
        }
        item.documentExpiryDate?.let {
            allEvents.add(
                TimelineEvent(
                    item = item,
                    eventType = "Document Renewal",
                    targetDate = it,
                    icon = Icons.Default.AssignmentLate,
                    color = if (it < now) StatusExpiredRed else StatusActiveGreen
                )
            )
        }
        item.customReminderDate?.let {
            allEvents.add(
                TimelineEvent(
                    item = item,
                    eventType = "Custom Reminder",
                    targetDate = it,
                    icon = Icons.Default.NotificationsActive,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    // Sort chronologically
    val sortedEvents = allEvents.sortedBy { it.targetDate }

    // Split into Upcoming vs Past
    val upcomingEvents = sortedEvents.filter { it.targetDate >= now }
    val pastEvents = sortedEvents.filter { it.targetDate < now }.reversed()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Timeline & Schedule",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
        if (sortedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Upcoming Deadlines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add warranty or return dates to your records to see them mapped on this timeline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (upcomingEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = "Upcoming Deadlines (${upcomingEvents.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(upcomingEvents) { event ->
                        TimelineEventCard(
                            event = event,
                            now = now,
                            onClick = { onNavigateToItem(event.item.id) }
                        )
                    }
                }

                if (pastEvents.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Past Expired Events (${pastEvents.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(pastEvents) { event ->
                        TimelineEventCard(
                            event = event,
                            now = now,
                            onClick = { onNavigateToItem(event.item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineEventCard(
    event: TimelineEvent,
    now: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diff = event.targetDate - now
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    val isPast = diff < 0

    val countdownText = when {
        isPast -> "Expired ${-days} days ago"
        days == 0L -> "Expires Today!"
        days == 1L -> "Expires Tomorrow!"
        else -> "In $days days"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("timeline_event_${event.item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(event.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.icon,
                    contentDescription = null,
                    tint = event.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.eventType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • ${VaultItem.formatDate(event.targetDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPast) StatusExpiredRed.copy(alpha = 0.15f) else event.color.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = countdownText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPast) StatusExpiredRed else event.color
                )
            }
        }
    }
}
