package com.freedom.audiodriver

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freedom.audiodriver.service.MainService
import com.freedom.audiodriver.ui.theme.AudioDriverTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.spr.jetpack_loading.components.indicators.PacmanIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )
    var isRunning by remember {
        mutableStateOf(
            MainService.isServiceRunningInForeground(
                context, MainService::class.java
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (permissionState.status.isGranted) {
                Column(
                    modifier = Modifier.fillMaxHeight(0.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        ),
                        modifier = Modifier.size(width = 240.dp, height = 120.dp),
                        border = BorderStroke(
                            2.dp, if (isRunning) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {

                            Row(
                                Modifier.padding(vertical = 12.dp),
                            ) {
                                Text(
                                    text = if (isRunning) "Service is running "
                                    else "Service is stopped ",
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    isRunning = MainService.isServiceRunningInForeground(
                                        context, MainService::class.java
                                    )
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxHeight(0.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        enabled = !isRunning, modifier = Modifier.fillMaxWidth(), onClick = {
                            val intent = Intent(context, MainService::class.java).apply {
                                action = MainService.Actions.START.name
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                            scope.launch {
                                isLoading = true
                                delay(1000)
                                isRunning = MainService.isServiceRunningInForeground(
                                    context, MainService::class.java
                                )
                                isLoading = false
                            }
                        }) {
                        Text("Start")
                    }
                    Button(
                        enabled = isRunning, modifier = Modifier.fillMaxWidth(), onClick = {
                            val intent = Intent(context, MainService::class.java).apply {
                                action = MainService.Actions.STOP.name
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                            scope.launch {
                                isLoading = true
                                delay(3000)
                                isRunning = MainService.isServiceRunningInForeground(
                                    context, MainService::class.java
                                )
                                isLoading = false
                            }
                        }) {
                        Text("Stop")
                    }
                }
            } else {
                val textToShow = if (permissionState.status.shouldShowRationale) {
                    "The microphone is important for this app. Please grant the permission."
                } else {
                    "Microphone permission required for this feature to be available. " +
                            "Please grant the permission"
                }
                Text(
                    textToShow, textAlign = TextAlign.Center
                )
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Request permission")
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                    )
                    // This intercepts all touch events
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume click */ }), contentAlignment = Alignment.Center
            ) {
                PacmanIndicator(
                    color = MaterialTheme.colorScheme.primary,
                )
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