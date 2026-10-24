package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.VaultDatabase
import com.example.data.VaultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object AlarmScheduler {

    fun scheduleItemReminders(context: Context, item: VaultItem) {
        if (!item.reminderEnabled || item.isArchived) {
            cancelItemReminders(context, item.id)
            return
        }

        val now = System.currentTimeMillis()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Check Return Deadline (Remind 3 days before and 1 day before)
        item.returnDeadlineDate?.let { returnDate ->
            if (returnDate > now) {
                val remindAt = returnDate - TimeUnit.DAYS.toMillis(2) // 2 days before
                if (remindAt > now) {
                    scheduleAlarm(
                        context,
                        alarmManager,
                        requestCode = (item.id * 10 + 1).toInt(),
                        triggerTimeMillis = remindAt,
                        itemId = item.id,
                        title = "Return Window Closing Soon",
                        message = "${item.title} return deadline is in 2 days! Prepare receipt and original packaging."
                    )
                }
            }
        }

        // 2. Check Warranty Expiry (Remind 14 days before)
        item.warrantyEndDate?.let { warrantyDate ->
            if (warrantyDate > now) {
                val remindAt = warrantyDate - TimeUnit.DAYS.toMillis(14) // 14 days before
                if (remindAt > now) {
                    scheduleAlarm(
                        context,
                        alarmManager,
                        requestCode = (item.id * 10 + 2).toInt(),
                        triggerTimeMillis = remindAt,
                        itemId = item.id,
                        title = "Warranty Expiring Soon",
                        message = "Warranty for ${item.title} ends in 14 days. Inspect device before coverage ends."
                    )
                }
            }
        }

        // 3. Check Document Expiry (Remind 30 days before)
        item.documentExpiryDate?.let { docDate ->
            if (docDate > now) {
                val remindAt = docDate - TimeUnit.DAYS.toMillis(30) // 30 days before
                if (remindAt > now) {
                    scheduleAlarm(
                        context,
                        alarmManager,
                        requestCode = (item.id * 10 + 3).toInt(),
                        triggerTimeMillis = remindAt,
                        itemId = item.id,
                        title = "Document Renewal Needed",
                        message = "${item.title} expires in 30 days. Plan your renewal submission."
                    )
                }
            }
        }

        // 4. Custom Reminder Date
        item.customReminderDate?.let { customDate ->
            if (customDate > now) {
                scheduleAlarm(
                    context,
                    alarmManager,
                    requestCode = (item.id * 10 + 4).toInt(),
                    triggerTimeMillis = customDate,
                    itemId = item.id,
                    title = "Reminder: ${item.title}",
                    message = if (item.notes.isNotBlank()) item.notes else "Scheduled reminder for ${item.title}"
                )
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerTimeMillis: Long,
        itemId: Long,
        title: String,
        message: String
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ITEM_ID", itemId)
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            // Fallback for security policies
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        }
    }

    fun cancelItemReminders(context: Context, itemId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (subId in 1..4) {
            val requestCode = (itemId * 10 + subId).toInt()
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    suspend fun rescheduleAll(context: Context) = withContext(Dispatchers.IO) {
        val db = VaultDatabase.getDatabase(context)
        val items = db.vaultDao().getAllItemsDirect()
        for (item in items) {
            if (item.reminderEnabled && !item.isArchived) {
                scheduleItemReminders(context, item)
            }
        }
    }
}
