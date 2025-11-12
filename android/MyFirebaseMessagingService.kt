package com.example.todoapp

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: send token to backend
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let { data ->
            if (data["sync"] == "true") {
                val work = OneTimeWorkRequestBuilder<SyncWorker>().build()
                WorkManager.getInstance(applicationContext).enqueue(work)
            }
        }
    }
}
