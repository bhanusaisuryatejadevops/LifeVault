package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra("ITEM_ID", -1L)
        val title = intent.getStringExtra("TITLE") ?: "LifeVault Reminder"
        val message = intent.getStringExtra("MESSAGE") ?: "You have an upcoming warranty or deadline."
        val notifId = if (itemId > 0) itemId.toInt() else System.currentTimeMillis().toInt()

        NotificationHelper.showDeadlineNotification(
            context = context,
            notificationId = notifId,
            title = title,
            message = message,
            itemId = itemId
        )
    }
}
