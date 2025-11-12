package com.example.todoapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID = "todo_reminders"
    private const val CHANNEL_NAME = "Todo Reminders"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for todo reminders"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleNotification(context: Context, task: Task) {
        // Create notification channel (idempotent)
        createNotificationChannel(context)
        
        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", task.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle(task.title)
            .setContentText(task.note ?: "Reminder")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Schedule using AlarmManager for exact timing
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = task.dueDate.time
        
        // Use setExactAndAllowWhileIdle for Android 6.0+ to ensure delivery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val alarmIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("notification", notification)
            }
            val alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, alarmPendingIntent)
        } else {
            // Fallback for older Android versions
            val alarmIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("notification", notification)
            }
            val alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, alarmPendingIntent)
        }
    }
    
    fun cancelNotification(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

// Placeholder classes - you'll need to implement these:
// data class Task(val id: String, val title: String, val note: String?, val dueDate: java.util.Date)
// class MainActivity : AppCompatActivity() { ... }
// class NotificationReceiver : BroadcastReceiver() { 
//     override fun onReceive(context: Context, intent: Intent) {
//         // Show the notification when alarm fires
//     }
// }
