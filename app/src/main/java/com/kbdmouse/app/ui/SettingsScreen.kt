package com.kbdmouse.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kbdmouse.app.MainViewModel

@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val sensitivity = vm.scrollSensitivity

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp)) {
        ScreenTopBar(onHome = onBack)

        Text(
            "Configuración",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
        )

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Sensibilidad scroll", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = sensitivity,
                onValueChange = { vm.updateScrollSensitivity(it) },
                valueRange = 0.1f..4f,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            )
            Text("%.1fx".format(sensitivity), style = MaterialTheme.typography.labelLarge)
        }
    }
}
