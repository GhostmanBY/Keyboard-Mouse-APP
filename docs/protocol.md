# Protocolo: teclado/mouse remoto vía TCP

Conexión TCP binaria, orientada a baja latencia por WiFi.

## Frame

```
offset  size  campo
0       2     magic   = "KM" (0x4B 0x4D)
2       1     version = 0x01
3       1     type    = ver tabla de mensajes
4       4     length  = largo del payload (big-endian)
8       N     payload
```

- Todo entero big-endian.
- El servidor aplica `TCP_NODELAY` (sin algoritmo de Nagle).
- El cliente debe enviar `HELLO` al conectar; el servidor responde `HELLO_ACK`.

## Tipos de mensaje

| type | nombre      | dirección        | payload                                              |
|-----:|-------------|------------------|------------------------------------------------------|
| 0x01 | HELLO       | cliente → server | versión cliente (1 byte)                             |
| 0x02 | HELLO_ACK   | server → cliente | versión servidor (1 byte)                            |
| 0x10 | KEY_DOWN    | cliente → server | HID usage (1 byte)                                   |
| 0x11 | KEY_UP      | cliente → server | HID usage (1 byte)                                   |
| 0x20 | MOUSE_MOVE  | cliente → server | dx int16, dy int16 (relativos, modo trackpad)        |
| 0x21 | MOUSE_BTN   | cliente → server | botón (1), estado (1)                                |
| 0x22 | SCROLL      | cliente → server | dx int16, dy int16                                   |
| 0x30 | PING        | server → cliente | seq u32, t0 u64 ms (monotónico del server)           |
| 0x31 | PONG        | cliente → server | eco exacto del payload del PING                      |
| 0x40 | DISCONNECT  | ambos            | —                                                    |

## Botones y estados del mouse

- Botón: `0` izquierdo, `1` derecho, `2` medio.
- Estado: `0` arriba (up), `1` abajo (down).

## Códigos de teclas

Se usan **códigos USB HID** (usage codes de 1 byte) para teclas estándar
(`0x04` A … `0x1D` Z, `0x1E` 1 … `0x27` 0, F1–F12, navegación, numpad) y
modificadores (`0xE0` Ctrl izq, `0xE1` Shift izq, `0xE2` Alt izq, `0xE3` Super
izq, y sus pares derechos `0xE4`–`0xE7`). Referencia: USB HID Usage Tables
(sección Keyboard/Keypad).

El servidor mapea HID → VK/scancode en Windows y HID → evdev en Linux.

## RTT (calidad de conexión)

El servidor envía un `PING` cada `ping_interval_ms` con un timestamp monotónico.
El cliente debe devolver `PONG` con el mismo payload. El servidor calcula el
RTT y lo muestra en consola. Si no llega tráfico en `heartbeat_timeout_s`, la
conexión se cierra (el cliente debería reconectar automáticamente).

## Ejemplo

HELLO:
```
4B 4D 01 01 00 00 00 01 01
```

MOUSE_MOVE dx=100, dy=-50:
```
4B 4D 01 20 00 00 00 04 00 64 FF CE
```
