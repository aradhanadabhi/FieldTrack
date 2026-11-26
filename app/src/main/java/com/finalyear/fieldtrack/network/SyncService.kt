package com.finalyear.fieldtrack.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.finalyear.fieldtrack.db.DatabaseHelper
import kotlin.concurrent.thread

/**
 * Demonstrates "Creating Android Background Services".
 * Runs as a foreground service, pushes any unsynced visits to the web API,
 * then stops itself.
 */
class SyncService : Service() {

    private val CHANNEL_ID = "fieldtrack_sync_channel"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification())

        thread {
            val db = DatabaseHelper.getInstance(applicationContext)
            val unsynced = db.getUnsyncedVisits()
            for (visit in unsynced) {
                val success = ApiClient.pushVisit(visit)
                if (success) db.markSynced(visit.id)
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Sync", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FieldTrack")
            .setContentText("Syncing visit data...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
    }
}
