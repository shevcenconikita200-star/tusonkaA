package com.example.tushenka;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.provider.OpenableColumns;
import android.content.ContentResolver;
import android.util.Base64;
import android.widget.Toast;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private static final int REQUEST_SAVE_FILE = 701;
    private static final int REQUEST_OPEN_FILE = 702;
    private WebView webView;
    private byte[] pendingBytes;
    private String pendingMime = "application/octet-stream";
    private ValueCallback<Uri[]> fileChooserCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return openExternal(request.getUrl().toString());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openExternal(url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/json", "application/octet-stream"});
                try { startActivityForResult(intent, REQUEST_OPEN_FILE); }
                catch (Exception e) { fileChooserCallback = null; callback.onReceiveValue(null); Toast.makeText(MainActivity.this, "Не удалось открыть выбор файла", Toast.LENGTH_LONG).show(); }
                return true;
            }
        });
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void saveFile(String filename, String mimeType, String base64Data) {
            try {
                pendingBytes = Base64.decode(base64Data, Base64.DEFAULT);
                pendingMime = (mimeType == null || mimeType.isEmpty()) ? "application/octet-stream" : mimeType;
                runOnUiThread(() -> {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(pendingMime);
                    intent.putExtra(Intent.EXTRA_TITLE, filename == null || filename.isEmpty() ? "download" : filename);
                    try { startActivityForResult(intent, REQUEST_SAVE_FILE); }
                    catch (Exception e) { pendingBytes = null; Toast.makeText(MainActivity.this, "Не удалось открыть сохранение файла", Toast.LENGTH_LONG).show(); }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Не удалось подготовить файл", Toast.LENGTH_LONG).show());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SAVE_FILE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingBytes != null) {
                try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                    if (out == null) throw new IOException("Не удалось открыть файл для записи");
                    out.write(pendingBytes);
                    out.flush();
                    Toast.makeText(this, "Файл сохранён", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Ошибка сохранения файла: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            pendingBytes = null;
            return;
        }
        if (requestCode == REQUEST_OPEN_FILE) {
            if (fileChooserCallback != null) {
                Uri[] result = (resultCode == RESULT_OK && data != null && data.getData() != null) ? new Uri[]{data.getData()} : null;
                fileChooserCallback.onReceiveValue(result);
                fileChooserCallback = null;
            }
        }
    }

    private boolean openExternal(String url) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if (scheme == null) return false;
        if (scheme.equals("file") || scheme.equals("javascript") || scheme.equals("about")) return false;
        if (scheme.equals("http") || scheme.equals("https") || scheme.equals("tel") || scheme.equals("geo") || scheme.equals("google.navigation") || scheme.equals("market")) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); return true; }
            catch (Exception e) { return true; }
        }
        if (scheme.equals("intent")) {
            try { startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME)); } catch (Exception ignored) { }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
