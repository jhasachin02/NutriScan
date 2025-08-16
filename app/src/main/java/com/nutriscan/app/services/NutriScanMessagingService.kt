package com.nutriscan.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nutriscan.app.R
import com.nutriscan.app.ui.MainActivity

class NutriScanMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "NutriScanMessaging"
        private const val CHANNEL_ID = "nutriscan_notifications"
        private const val CHANNEL_NAME = "NutriScan Notifications"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Send token to your server or save locally
        sendTokenToServer(token)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Handle different types of notifications
        when (remoteMessage.data["type"]) {
            "meal_reminder" -> handleMealReminder(remoteMessage)
            "goal_progress" -> handleGoalProgress(remoteMessage)
            "health_tip" -> handleHealthTip(remoteMessage)
            "recipe_suggestion" -> handleRecipeSuggestion(remoteMessage)
            else -> handleDefaultNotification(remoteMessage)
        }
    }
    
    private fun handleMealReminder(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Meal Time!"
        val body = remoteMessage.data["body"] ?: "Don't forget to log your meal"
        
        showNotification(
            title = title,
            body = body,
            icon = R.drawable.ic_restaurant,
            channelId = CHANNEL_ID
        )
    }
    
    private fun handleGoalProgress(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Great Progress!"
        val body = remoteMessage.data["body"] ?: "You're on track with your nutrition goals"
        
        showNotification(
            title = title,
            body = body,
            icon = R.drawable.ic_trending_up,
            channelId = CHANNEL_ID
        )
    }
    
    private fun handleHealthTip(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Health Tip"
        val body = remoteMessage.data["body"] ?: "Here's a nutrition tip for you!"
        
        showNotification(
            title = title,
            body = body,
            icon = R.drawable.ic_lightbulb,
            channelId = CHANNEL_ID
        )
    }
    
    private fun handleRecipeSuggestion(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Recipe Suggestion"
        val body = remoteMessage.data["body"] ?: "We found a healthy recipe for you!"
        
        showNotification(
            title = title,
            body = body,
            icon = R.drawable.ic_menu_book,
            channelId = CHANNEL_ID
        )
    }
    
    private fun handleDefaultNotification(remoteMessage: RemoteMessage) {
        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "NutriScan",
                body = notification.body ?: "You have a new notification",
                icon = R.drawable.ic_notifications,
                channelId = CHANNEL_ID
            )
        }
    }
    
    private fun showNotification(
        title: String,
        body: String,
        icon: Int,
        channelId: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(getColor(R.color.primary))
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for meal reminders, health tips, and nutrition updates"
                enableLights(true)
                lightColor = getColor(R.color.primary)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun sendTokenToServer(token: String) {
        // TODO: Implement sending token to your backend server
        // This would typically involve making an API call to register the token
        Log.d(TAG, "Sending token to server: $token")
        
        // Example of what you might do:
        /*
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.registerFCMToken(
                    userId = getCurrentUserId(),
                    token = token
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send token to server", e)
            }
        }
        */
    }
}
