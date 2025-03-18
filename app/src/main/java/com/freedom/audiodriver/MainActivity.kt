package com.freedom.audiodriver

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.freedom.audiodriver.service.MainService
import com.freedom.audiodriver.ui.theme.AudioDriverTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioDriverTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Main(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Main(modifier: Modifier = Modifier) {
    Column (
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current
        val permissionState = rememberPermissionState(
            Manifest.permission.RECORD_AUDIO
        )

        if (permissionState.status.isGranted) {
            Button(
                onClick = {
                    val isServiceRunning = MainService.isServiceRunningInForeground(
                        context,
                        MainService::class.java
                    )
                    val status = if (isServiceRunning) "Running" else "Not running"
                    val toast = Toast.makeText(context, status, Toast.LENGTH_SHORT)
                    toast.show()
                }
            ) {
                Text("Status")
            }
            Button(
                onClick = {
                    val intent = Intent(context, MainService::class.java).apply {
                        action = MainService.Actions.START.name
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }
            ) {
                Text("Start")
            }
            Button(
                onClick = {
                    val intent = Intent(context, MainService::class.java).apply {
                        action = MainService.Actions.STOP.name
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }
            ) {
                Text("Stop")
            }
        } else {
            val textToShow = if (permissionState.status.shouldShowRationale) {
                "The microphone is important for this app. Please grant the permission."
            } else {
                "Microphone permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(
                textToShow,
                textAlign = TextAlign.Center
            )
            Button (onClick = { permissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    AudioDriverTheme {
        Main()
    }
}