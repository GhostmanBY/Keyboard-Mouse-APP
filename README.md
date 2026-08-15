# Servidor teclado/mouse vía WiFi

Usa tu teléfono como **teclado y mouse** del PC. Este repositorio contiene el
servidor (lo que corre en el PC) y, en el futuro, la app Android.

- **Servidor:** Python, objetivo Windows, se compila a `server.exe` (PyInstaller).
  Inyección con `user32.SendInput` vía ctypes — **cero dependencias en Windows**.
  En Linux usa uinput (`evdev`) de forma opcional.
- **Conexión:** TCP con `TCP_NODELAY` y mensajes binarios mínimos para baja
  latencia por WiFi. RTT medido por ping para ver la calidad del enlace.
- **Mouse:** modo trackpad (deltas relativos) con clic izquierdo/derecho y rueda.
- **Teclado:** código USB HID → mapeado a VK/scancode (Windows) o evdev (Linux).

## Estructura

```
server/
├── run.py                 # entrada del servidor
├── config.json            # puerto, sensibilidad, etc.
├── client_tool.py         # cliente de prueba (stdlib puro)
├── build.spec             # PyInstaller (.exe)
├── udev/                  # regla uinput para Linux
├── .github/workflows/     # CI: compila el .exe en Windows
├── kbdmouse/
│   ├── protocol.py        # frames binarios TCP
│   ├── keymap.py          # HID → VK / evdev
│   ├── config.py
│   ├── net/server.py      # asyncio + TCP_NODELAY + RTT
│   └── input/             # windows.py, linux.py, null.py
└── tests/
docs/protocol.md           # especificación del protocolo
```

## Ejecutar en desarrollo

Requiere [uv](https://docs.astral.sh/uv/).

```bash
cd server
uv sync                          # crea .venv e instala dependencias
uv run python run.py             # usa config.json
uv run python run.py otra.json   # config alternativa
uv run pytest                    # tests
```

Para el inyector Linux (uinput) se necesita el extra `linux`:

```bash
uv sync --extra linux
```

Prueba con el cliente simulado (desde otro terminal):

```bash
uv run python client_tool.py 127.0.0.1
> move 40 -15
> btn left down
> key 0x1E     # tecla '1'
> key 0xE1 down   # mantener Shift
```

## Compilar el .exe (Windows)

PyInstaller no compila de forma cruzada desde Linux. Opciones:

1. **GitHub Actions (recomendado):** sube el repo, entra en
   `Actions → Build Windows server (.exe) → Run workflow`, y descarga el artefacto
   `kbdmouse-server.zip`.
2. **En tu máquina Windows:**
   ```bat
   pip install pyinstaller
   pyinstaller build.spec
   ```
   El resultado queda en `dist/kbdmouse-server/`.

## Uso en Windows

1. Descomprime el zip y ejecuta `kbdmouse-server.exe`.
2. La consola muestra la **IP WiFi** y el puerto (por defecto `8765`).
3. Abre en el firewall de Windows el puerto TCP (o elige uno nuevo en
   `config.json` antes de compilar).
4. Conecta la app del teléfono a `IP:puerto`.

## Configuración

| campo                | descripción                                     |
|----------------------|-------------------------------------------------|
| `host`               | interfaz a escuchar (`0.0.0.0` = todas)         |
| `port`               | puerto TCP (predeterminado 8765)                |
| `injector`           | `auto` (según SO), `windows`, `linux`, `null`   |
| `mouse_sensitivity`  | multiplicador de deltas del trackpad            |
| `scroll_sensitivity` | multiplicador de la rueda                       |
| `ping_interval_ms`   | intervalo de ping/RTT (ms)                      |
| `heartbeat_timeout_s`| cierra conexión sin tráfico tras X s            |

## Linux (secundario)

Requiere el paquete `evdev` y acceso a `/dev/uinput`. Regla udev incluida:

```bash
sudo cp udev/99-phone-kbm.rules /etc/udev/rules.d/
sudo udevadm control --reload-rules && sudo udevadm trigger
sudo usermod -aG input $USER   # y reinicia sesión
```

Con `"injector": "linux"` el servidor crea un dispositivo virtual
teclado+mouse (funciona en Wayland y X11).
