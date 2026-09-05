package com.sgmando.instantoffer;

import android.app.Activity;
import android.graphics.Color;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import android.os.Bundle;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    public class AndroidAppBridge {
        @JavascriptInterface
        public void downloadText(String fileName, String text) {
            runOnUiThread(() -> saveResultsFile(fileName, text));
        }
    }

    private void saveResultsFile(String fileName, String text) {
        String cleanName = (fileName == null || fileName.trim().isEmpty()) ? "Instant-Offer-Results.txt" : fileName;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, cleanName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Instant Offer Calculator");
                values.put(MediaStore.Downloads.IS_PENDING, 1);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("Could not create download file");

                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out == null) throw new Exception("Could not open download file");
                    out.write(text.getBytes(StandardCharsets.UTF_8));
                }

                ContentValues done = new ContentValues();
                done.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(uri, done, null, null);
                Toast.makeText(this, "Saved to Downloads/Instant Offer Calculator", Toast.LENGTH_LONG).show();
            } else {
                File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Instant Offer Calculator");
                if (!dir.exists() && !dir.mkdirs()) throw new Exception("Could not create folder");
                File file = new File(dir, cleanName);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(text.getBytes(StandardCharsets.UTF_8));
                }
                Toast.makeText(this, "Results saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not save results", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#0B3B79"));
        window.setNavigationBarColor(Color.parseColor("#0B3B79"));

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        webView.addJavascriptInterface(new AndroidAppBridge(), "AndroidApp");
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(Color.parseColor("#F4F7FB"));
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
