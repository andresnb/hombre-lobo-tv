package com.andresnb.hombrelobo.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

// La app solo abre el juego (la web) en pantalla completa: los cambios del juego llegan solos.
// Además presta la voz de Android para el narrador y avisa cuando hay una versión nueva de la app.
public class MainActivity extends Activity {
    static final String JUEGO = "https://hybrid-werewolf.web.app/tv";
    static final String VERSION = "https://github.com/andresnb/hombre-lobo-tv/releases/latest/download/version.json";

    WebView web;
    TextToSpeech voz;

    @Override
    protected void onCreate(Bundle guardado) {
        super.onCreate(guardado);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        voz = new TextToSpeech(this, estado -> {
            if (estado == TextToSpeech.SUCCESS) voz.setLanguage(new Locale("es", "US"));
        });

        web = new WebView(this);
        WebSettings ajustes = web.getSettings();
        ajustes.setJavaScriptEnabled(true);
        ajustes.setDomStorageEnabled(true);
        ajustes.setMediaPlaybackRequiresUserGesture(false); // el control remoto no "toca": que suene sin pedirlo
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new Puente(), "HombreLoboTV");
        setContentView(web);
        if (guardado == null) web.loadUrl(JUEGO); else web.restoreState(guardado);

        buscarVersion();
    }

    // Lo que la web puede pedirle a la app: window.HombreLoboTV.hablar(texto, volumen 0..1).
    class Puente {
        @JavascriptInterface
        public void hablar(String texto, float volumen) {
            Bundle opciones = new Bundle();
            opciones.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volumen);
            voz.speak(texto, TextToSpeech.QUEUE_FLUSH, opciones, "narrador");
        }
    }

    void buscarVersion() {
        new Thread(() -> {
            try (InputStream entrada = new URL(VERSION).openStream(); Scanner s = new Scanner(entrada).useDelimiter("\\A")) {
                JSONObject ultima = new JSONObject(s.next());
                if (ultima.getInt("versionCode") > BuildConfig.VERSION_CODE)
                    runOnUiThread(() -> ofrecer(ultima.optString("versionName"), ultima.optString("url")));
            } catch (Exception sinRed) {
                // Sin internet o sin versiones publicadas: se juega con la que hay.
            }
        }).start();
    }

    void ofrecer(String nombre, String url) {
        if (isFinishing()) return;
        new AlertDialog.Builder(this)
                .setTitle("Hay una versión nueva")
                .setMessage("Versión " + nombre + " (tienes la " + BuildConfig.VERSION_NAME + "). ¿Actualizar ahora?")
                .setPositiveButton("Actualizar", (d, b) -> descargar(url))
                .setNegativeButton("Después", null)
                .show();
    }

    void descargar(String url) {
        Toast.makeText(this, "Descargando la versión nueva…", Toast.LENGTH_LONG).show();
        new Thread(() -> {
            File apk = new File(getCacheDir(), "apk/hombre-lobo-tv.apk");
            apk.getParentFile().mkdirs();
            try (InputStream entrada = new URL(url).openStream(); OutputStream salida = new FileOutputStream(apk)) {
                byte[] trozo = new byte[64 * 1024];
                for (int n; (n = entrada.read(trozo)) > 0; ) salida.write(trozo, 0, n);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "No se pudo descargar. Intenta más tarde.", Toast.LENGTH_LONG).show());
                return;
            }
            runOnUiThread(() -> {
                Uri archivo = FileProvider.getUriForFile(this, getPackageName() + ".archivos", apk);
                startActivity(new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(archivo, "application/vnd.android.package-archive")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK));
            });
        }).start();
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        web.saveState(estado);
    }

    @Override
    protected void onDestroy() {
        voz.shutdown();
        web.destroy();
        super.onDestroy();
    }
}
