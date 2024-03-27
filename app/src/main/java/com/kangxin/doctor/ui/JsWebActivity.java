package com.kangxin.doctor.ui;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.kangxin.doctor.R;

public class JsWebActivity extends ComponentActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        start();
    }

    public void start() {
        WebView webView = findViewById(R.id.vWebView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JsJava(),"jsObject");
        webView.loadUrl("file:///android_asset/mdthz-patient-agreement.html");
    }

}
