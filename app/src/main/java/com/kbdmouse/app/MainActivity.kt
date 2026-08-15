package com.kbdmouse.app

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kbdmouse.app.ui.ConnectScreen
import com.kbdmouse.app.ui.KeyboardScreen
import com.kbdmouse.app.ui.SettingsScreen
import com.kbdmouse.app.ui.TrackpadScreen
import com.kbdmouse.app.ui.theme.KbdMouseTheme
import com.kbdmouse.app.net.ConnectionState

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            KbdMouseTheme {
                MainScreen(viewModel)
            }
        }
    }
}

private val TABS = listOf(
    Triple(0, "Conexión", "⚡"),
    Triple(1, "Trackpad", "✋"),
    Triple(2, "Teclado", "⌨"),
)

@Composable
private fun MainScreen(vm: MainViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val connectionState by vm.state.collectAsState()
    val connected = connectionState is ConnectionState.Connected

    fun goTab(index: Int) {
        if (index == 0 || connected) {
            tab = index
            showSettings = false
        } else {
            Toast.makeText(context, "Conéctate primero", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(tab) {
        activity?.requestedOrientation = if (tab == 1 || tab == 2) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val fullscreen = tab == 1 || tab == 2

    Scaffold(
        bottomBar = {
            if (!fullscreen) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for ((index, label, icon) in TABS) {
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (tab == index) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface,
                                )
                                .clickable { goTab(index) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(icon, fontWeight = FontWeight.Bold)
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (tab == index) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (tab) {
                1 -> TrackpadScreen(
                    vm,
                    onHome = {
                        tab = 0
                        showSettings = false
                    },
                    onKeyboard = { goTab(2) },
                )
                2 -> KeyboardScreen(
                    vm,
                    onHome = {
                        tab = 0
                        showSettings = false
                    },
                    onKeyboard = { goTab(1) },
                )
                else -> if (showSettings) {
                    SettingsScreen(vm, onBack = { showSettings = false })
                } else {
                    ConnectScreen(vm, onSettings = { showSettings = true })
                }
            }
        }
    }
}
