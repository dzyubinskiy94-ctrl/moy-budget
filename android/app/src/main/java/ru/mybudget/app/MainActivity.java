package ru.mybudget.app;

import android.app.Activity;
import android.content.Context;
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
import android.graphics.Bitmap;
import org.json.JSONObject;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Offline finance app wrapper. Data remains in Android WebView DOM storage. */
public final class MainActivity extends Activity {
    private static final int PICK_BACKUP = 201;
    private static final int SAVE_BACKUP = 202;
    private static final String APP_URL = "https://appassets.androidplatform.net/assets/index.html";
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private String pendingData;
    private Intent pendingWidgetAction;
    private boolean webViewReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pendingWidgetAction = getIntent();

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
        settings.setDatabaseEnabled(true);
        webView.addJavascriptInterface(new BackupBridge(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (APP_URL.equals(url)) {
                    webViewReady = true;
                    dispatchWidgetAction();
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Do not navigate to untrusted content with JavaScript bridge enabled.
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
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
            }
        });
        webView.loadUrl(APP_URL);
    }

    public class BackupBridge {
        @JavascriptInterface
        public void syncWidget(String widgetJson) {
            // Called from the trusted local WebView; never stores transaction data.
            QuickExpenseWidgetProvider.saveShortcuts(MainActivity.this, widgetJson);
        }

        @JavascriptInterface
        public void saveFile(String fileName, String contents, String mimeType) {
            runOnUiThread(() -> {
                pendingData = contents;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(mimeType.startsWith("text/csv") ? "text/csv" : "application/json");
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                try {
                    startActivityForResult(intent, SAVE_BACKUP);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Не удалось открыть сохранение", Toast.LENGTH_LONG).show();
                    pendingData = null;
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        pendingWidgetAction = intent;
        dispatchWidgetAction();
    }

    private void dispatchWidgetAction() {
        if (!webViewReady || pendingWidgetAction == null) return;
        Intent intent = pendingWidgetAction;
        pendingWidgetAction = null;
        if (!QuickExpenseWidgetProvider.ACTION_WIDGET_CLICK.equals(intent.getAction())) return;
        final String mode = intent.getStringExtra(QuickExpenseWidgetProvider.EXTRA_MODE);
        final String arg = intent.getStringExtra(QuickExpenseWidgetProvider.EXTRA_ARGUMENT);
        final String script = "window.MoyBudgetWidget&&window.MoyBudgetWidget.handle("
            + JSONObject.quote(mode == null ? "add" : mode) + ","
            + JSONObject.quote(arg == null ? "" : arg) + ");";
        webView.evaluateJavascript(script, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_BACKUP) {
            if (fileCallback != null) {
                Uri[] result = resultCode == RESULT_OK && data != null && data.getData() != null
                    ? new Uri[] { data.getData() } : null;
                fileCallback.onReceiveValue(result);
                fileCallback = null;
            }
        } else if (requestCode == SAVE_BACKUP) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingData != null) {
                try (OutputStream stream = getContentResolver().openOutputStream(data.getData())) {
                    if (stream == null) throw new IllegalStateException("Cannot open output file");
                    stream.write(pendingData.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "Файл сохранён", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Ошибка записи файла", Toast.LENGTH_LONG).show();
                }
            }
            pendingData = null;
        }
    }

    @Override
    public void onBackPressed() {
        webView.evaluateJavascript("(function(){var m=document.getElementById('modal');if(m&&m.classList.contains('open')){document.querySelector('[data-action=close]')?.click();return true;}return false;})()", val -> {
            if (!"true".equals(val)) MainActivity.super.onBackPressed();
        });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
