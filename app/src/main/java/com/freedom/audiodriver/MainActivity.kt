package com.freedom.audiodriver

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            android.Manifest.permission.RECORD_AUDIO
        )
        var running by remember { mutableStateOf(false) }

        if (permissionState.status.isGranted) {
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
                    running = true
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
                    running = false
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