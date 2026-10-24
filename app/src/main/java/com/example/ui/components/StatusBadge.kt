package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ItemStatus
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusExpiredRed
import com.example.ui.theme.StatusExpiringAmber
import com.example.ui.theme.StatusNeutralBlue

@Composable
fun StatusBadge(
    status: ItemStatus,
    daysRemaining: Long? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        ItemStatus.RETURN_URGENT -> Triple(
            StatusExpiringAmber.copy(alpha = 0.18f),
            StatusExpiringAmber,
            Icons.Default.Warning
        )
        ItemStatus.EXPIRING_SOON -> Triple(
            StatusExpiringAmber.copy(alpha = 0.15f),
            StatusExpiringAmber,
            Icons.Default.AccessTime
        )
        ItemStatus.ACTIVE -> Triple(
            StatusActiveGreen.copy(alpha = 0.15f),
            StatusActiveGreen,
            Icons.Default.CheckCircle
        )
        ItemStatus.EXPIRED -> Triple(
            StatusExpiredRed.copy(alpha = 0.15f),
            StatusExpiredRed,
            Icons.Default.Dangerous
        )
        ItemStatus.PERMANENT -> Triple(
            StatusNeutralBlue.copy(alpha = 0.15f),
            StatusNeutralBlue,
            Icons.Default.CheckCircle
        )
    }

    val labelText = when {
        status == ItemStatus.RETURN_URGENT && daysRemaining != null -> "$daysRemaining days to return"
        status == ItemStatus.EXPIRING_SOON && daysRemaining != null -> "$daysRemaining days left"
        status == ItemStatus.ACTIVE && daysRemaining != null -> "$daysRemaining days coverage"
        status == ItemStatus.EXPIRED -> "Coverage Expired"
        else -> status.label
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = labelText,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
