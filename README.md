# Hombre Lobo · app para la TV

App de Android TV / Google TV que abre el juego [hybrid-werewolf.web.app/tv](https://hybrid-werewolf.web.app/tv) en pantalla completa.
El juego vive en la web, así que sus cambios llegan solos. La app solo agrega la voz del narrador (la de Android) y el aviso de versión nueva.

**Instalar en la TV:** abre la app *Downloader* y escribe `hybrid-werewolf.web.app/apk`.

**Versiones:** cada cambio en `main` compila la app y publica una versión nueva (1.0, 1.1, …). Al abrirla, la app compara su número con `version.json` de la última versión y ofrece actualizar.

**Ícono y banner:** se dibujan en `arte/` (SVG) y se convierten con `node arte/dibujar.cjs <ruta a sharp>`.
