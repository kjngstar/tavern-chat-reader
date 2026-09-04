package io.github.kjngstar.tavernchatreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.app.Activity;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 904;
    private WebView webView;
    private ValueCallback<Uri[]> pendingFileCallback;
    private volatile boolean readerActive;
    private volatile boolean ttsPlaying;
    private volatile String volumeKeyMode = "auto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        webView = new WebView(this);
        configureWebView();
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setTextZoom(100);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.addJavascriptInterface(new ReaderBridge(), "AndroidReader");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (pendingFileCallback != null) pendingFileCallback.onReceiveValue(null);
                pendingFileCallback = callback;
                Intent picker = params.createIntent();
                picker.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(picker, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception ignored) {
                    pendingFileCallback = null;
                    return false;
                }
            }
        });
    }

    private void enterImmersiveMode() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        }
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && pendingFileCallback != null) {
            Uri[] files = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            pendingFileCallback.onReceiveValue(files);
            pendingFileCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN;
        boolean shouldTurnPage = readerActive
                && !"volume".equals(volumeKeyMode)
                && ("page".equals(volumeKeyMode) || !ttsPlaying);
        if (isVolumeKey && shouldTurnPage && webView != null) {
            int direction = keyCode == KeyEvent.KEYCODE_VOLUME_UP ? -1 : 1;
            webView.evaluateJavascript(
                    "window.tavernReaderVolumePage && window.tavernReaderVolumePage(" + direction + ")",
                    null
            );
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** JavaScript bridge for the trusted offline reader page bundled in this APK. */
    private final class ReaderBridge {
        @JavascriptInterface
        public void setReaderActive(boolean active) {
            readerActive = active;
        }

        @JavascriptInterface
        public void setTtsPlaying(boolean playing) {
            ttsPlaying = playing;
        }

        @JavascriptInterface
        public void setVolumeKeyMode(String mode) {
            if ("page".equals(mode) || "volume".equals(mode) || "auto".equals(mode)) {
                volumeKeyMode = mode;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterImmersiveMode();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
