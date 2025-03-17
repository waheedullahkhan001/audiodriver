package com.freedom.audiodriver.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MainService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> start()
            Actions.STOP.name -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        // Start the service
    }

    private fun stop() {
        stopSelf()
    }

    enum class Actions {
        START,
        STOP
    }
}