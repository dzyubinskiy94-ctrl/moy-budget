package ru.mybudget.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** All records are held by local WebView storage; no account or remote backend. */
public final class MainActivity extends Activity {
    private static final int PICK_BACKUP = 201;
    private static final int SAVE_BACKUP = 202;
    private static final String APP_URL = "https://appassets.androidplatform.net/assets/index.html";
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private String pendingData;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();

        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.addJavascriptInterface(new BackupBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Prevent external sites accessing the JavaScript file-export bridge.
                return !"appassets.androidplatform.net".equals(request.getUrl().getHost());
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("application/json");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent, PICK_BACKUP);
                    return true;
                } catch (Exception ex) {
                    fileCallback = null;
                    Toast.makeText(MainActivity.this, "Не удалось выбрать резервную копию", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
        webView.loadUrl(APP_URL);
    }

    public final class BackupBridge {
        @JavascriptInterface
        public void saveFile(String fileName, String content, String mimeType) {
            runOnUiThread(() -> {
                pendingData = content;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(mimeType != null && mimeType.startsWith("text/csv") ? "text/csv" : "application/json");
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                try {
                    startActivityForResult(intent, SAVE_BACKUP);
                } catch (Exception ex) {
                    pendingData = null;
                    Toast.makeText(MainActivity.this, "Не удалось открыть сохранение файла", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_BACKUP) {
            if (fileCallback != null) {
                Uri[] result = (resultCode == RESULT_OK && data != null && data.getData() != null)
                    ? new Uri[] { data.getData() } : null;
                fileCallback.onReceiveValue(result);
                fileCallback = null;
            }
        } else if (requestCode == SAVE_BACKUP) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingData != null) {
                try (OutputStream stream = getContentResolver().openOutputStream(data.getData())) {
                    if (stream == null) throw new IllegalStateException("Output file is not available");
                    stream.write(pendingData.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "Резервная копия сохранена", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(this, "Ошибка сохранения файла", Toast.LENGTH_LONG).show();
                }
            }
            pendingData = null;
        }
    }

    @Override
    public void onBackPressed() {
        webView.evaluateJavascript(
            "(function(){var m=document.getElementById('modal');if(m&&m.classList.contains('open')){var c=document.querySelector('[data-action=close]');if(c)c.click();return true;}return false;})()",
            result -> {if (!"true".equals(result)) MainActivity.super.onBackPressed();}
        );
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
