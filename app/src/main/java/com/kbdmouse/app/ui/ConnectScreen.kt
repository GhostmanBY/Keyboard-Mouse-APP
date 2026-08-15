package com.kbdmouse.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kbdmouse.app.MainViewModel
import com.kbdmouse.app.net.ConnectionState

@Composable
fun ConnectScreen(vm: MainViewModel, onSettings: () -> Unit) {
    val state by vm.state.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Spacer(Modifier.height(32.dp))
        Text("KbdMouse", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Teclado y mouse del PC por WiFi",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = vm.host,
            onValueChange = { vm.host = it },
            label = { Text("IP del servidor") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = vm.port,
            onValueChange = { vm.port = it },
            label = { Text("Puerto") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { vm.toggleConnection() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(if (vm.isConnected) "Desconectar" else "Conectar")
        }
        Spacer(Modifier.height(16.dp))

        Text(
            state.label(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "El servidor imprime su IP WiFi y el puerto al arrancar.\n" +
                "El teléfono debe estar en la misma red.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        }

        RoundIconButton(
            icon = "⚙",
            onClick = onSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
        )
    }
}

private fun ConnectionState.label(): String = when (this) {
    ConnectionState.Disconnected -> "Desconectado"
    ConnectionState.Connecting -> "Conectando…"
    is ConnectionState.Connected -> "Conectado a ${host}:${port}"
    is ConnectionState.Reconnecting -> "Reconectando (intento $attempt)"
}
