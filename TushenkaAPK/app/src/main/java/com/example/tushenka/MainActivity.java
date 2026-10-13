
package com.example.tushenka;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.view.Window;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, WebResourceRequest request) {
                return openExternal(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, String url) {
                return openExternal(url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    private boolean openExternal(String url) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();

        if (scheme == null) return false;

        if (scheme.equals("file") ||
            scheme.equals("javascript") ||
            scheme.equals("about")) {
            return false;
        }

        if (scheme.equals("http") ||
            scheme.equals("https") ||
            scheme.equals("tel") ||
            scheme.equals("geo") ||
            scheme.equals("google.navigation") ||
            scheme.equals("market")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            } catch (Exception e) {
                return true;
            }
        }

        if (scheme.equals("intent")) {
            try {
                Intent intent = Intent.parseUri(
                    url, Intent.URI_INTENT_SCHEME);
                startActivity(intent);
            } catch (Exception ignored) {
            }
            return true;
        }

        return false;
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
