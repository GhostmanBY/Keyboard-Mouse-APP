# AGENTS.md

Guía para agentes de IA que trabajen en este proyecto.

## Visión general

App para usar el teléfono como **teclado y mouse** del PC a través de WiFi.
Arquitectura cliente-servidor con protocolo TCP binario de baja latencia.

- **Este repositorio/carpeta** (`Keyboard&Mouse`) es el **servidor**.
- La carpeta **`Keyboard&Mouse-APP`** (hermana, al mismo nivel) es el **cliente
  Android nativo (Kotlin)**. No es parte de este repo; es un proyecto separado.

El protocolo de red es compartido y está documentado en `docs/protocol.md`.
El cliente Android usa los mismos códigos de tecla USB HID y el mismo
formato de frames.

## Servidor (`Keyboard&Mouse/server`)

Python, objetivo principal **Windows** (compilado a `.exe` con PyInstaller);
Linux es secundario y opcional.

| Aspecto            | Detalle                                                            |
|--------------------|--------------------------------------------------------------------|
| Conexión           | TCP, `TCP_NODELAY`, frames binarios mínimos, ping/RTT + heartbeat  |
| Inyección Windows  | `user32.SendInput` vía `ctypes` — cero dependencias en runtime     |
| Inyección Linux    | `uinput` vía `evdev` (extra opcional `linux`)                      |
| Teclado            | El cliente envía códigos USB HID; el servidor mapea HID→VK (Win) / HID→evdev (Linux) |
| Mouse              | Modo trackpad: deltas relativos + botones izq/der + rueda          |
| Entorno            | `uv` (`uv sync`, `uv run`)                                         |
| Tests              | `pytest` (`uv run pytest`), 20 tests                               |

### Estructura del servidor

```
server/
├── run.py                 # entrada (python run.py [config.json])
├── client_tool.py         # cliente de prueba interactivo (stdlib puro)
├── config.json            # puerto, sensibilidad, inyector, etc.
├── pyproject.toml         # deps con uv (evdev como extra "linux")
├── build.spec             # PyInstaller para el .exe (build en Windows)
├── udev/99-phone-kbm.rules# regla uinput para Linux
├── .github/workflows/     # CI que compila el .exe en Windows
├── kbdmouse/
│   ├── protocol.py        # frames binarios + builders/parsers
│   ├── keymap.py          # tablas HID → VK / evdev
│   ├── config.py          # carga de config.json con defaults
│   ├── net/server.py      # KbdMouseServer asyncio, TCP_NODELAY, RTT
│   └── input/             # base.py, windows.py, linux.py, null.py, __init__.py (fábrica)
└── tests/                 # test_protocol, test_keymap, test_server
```

### Protocolo (resumen)

Frame: `magic "KM"(2) | version(1) | type(1) | length BE(4) | payload`.

Mensajes principales: `HELLO`/`HELLO_ACK`, `KEY_DOWN`, `KEY_UP`
(1 byte HID), `MOUSE_MOVE` (dx,dy int16), `MOUSE_BTN` (btn,state),
`SCROLL` (dx,dy int16), `PING`/`PONG` (seq u32 + t0 u64 ms), `DISCONNECT`.
Botones mouse: 0=izq, 1=der, 2=medio. Detalle completo en `docs/protocol.md`.

### Comandos habituales

```bash
cd server
uv sync                  # entorno base (pytest, sin extras)
uv sync --extra linux    # añade evdev (inyector uinput)
uv run python run.py     # arranca el servidor
uv run python run.py <config.json>
uv run pytest            # 20 tests
uv run python client_tool.py <host> [puerto]   # cliente de prueba
```

Nota: el inyector `auto` en Linux cae a `NullInjector` (simulado) con aviso si
evdev no está instalado; nunca aborta el arranque.

### .exe

PyInstaller no compila en Linux. El `.exe` se genera en Windows (o vía el
workflow de GitHub Actions). Especificación en `build.spec` (modo `onedir`).
El `.exe` imprime la IP WiFi y el puerto al arrancar; hay que abrir el puerto
TCP en el firewall de Windows.

## Cliente Android (`Keyboard&Mouse-APP`)

- Carpeta hermana al mismo nivel, fuera de este repo.
- Kotlin nativo. Debe implementar el protocolo de `docs/protocol.md`
  (HELLO al conectar, responder PING con PONG, `TCP_NODELAY`).
- Funciones: trackpad (deltas relativos), botones izq/der, teclado virtual
  que envía códigos USB HID, y mostrar el RTT.

## Convenciones

- Python: tipado moderno (`from __future__ import annotations`), sin
  dependencias en runtime para Windows (ctypes puro).
- Sin comentarios de relleno; código autoexplicativo.
- El protocolo compartido NO debe romperse sin actualizar también el cliente
  Android y `docs/protocol.md`.
