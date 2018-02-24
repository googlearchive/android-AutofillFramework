/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.autofill.app.commoncases;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.android.autofill.app.R;

import static com.example.android.autofill.app.Util.DEBUG;
import static com.example.android.autofill.app.Util.TAG;

public class WebViewSignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_webview_activity);

        WebView webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webView.setWebViewClient(new WebViewClient());
        webSettings.setJavaScriptEnabled(true);

        String url = getIntent().getStringExtra("url");
        if (url == null) {
            url = "file:///android_res/raw/sample_form.html";
        }
        if (DEBUG) Log.d(TAG, "Clearing WebView data");
        webView.clearHistory();
        webView.clearFormData();
        webView.clearCache(true);
        Log.i(TAG, "Loading URL " + url);
        webView.loadUrl(url);
    }
}