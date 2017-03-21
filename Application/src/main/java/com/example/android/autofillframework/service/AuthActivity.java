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
package com.example.android.autofillframework.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.autofill.AutoFillId;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.autofillframework.R;

import java.util.Map;

import static android.view.autofill.AutoFillManager.EXTRA_ASSIST_STRUCTURE;
import static android.view.autofill.AutoFillManager.EXTRA_AUTHENTICATION_RESULT;
import static com.example.android.autofillframework.CommonUtil.EXTRA_DATASET_NAME;
import static com.example.android.autofillframework.CommonUtil.EXTRA_FOR_RESPONSE;
import static com.example.android.autofillframework.CommonUtil.TAG;

/**
 * This Activity controls the UI for logging in to the Autofill service.
 * It is launched when an Autofill Response or specific Dataset within the Response requires
 * authentication to access. It bundles the result in an Intent.
 */
public class AuthActivity extends Activity {

    // Unique id for dataset intents.
    private static int sDatasetPendingIntentId = 0;

    private EditText mMasterPassword;
    private Button mCancel;
    private Button mLogin;
    private Intent mReplyIntent;

    static IntentSender getAuthIntentSenderForResponse(Context context) {
        final Intent intent = new Intent(context, AuthActivity.class);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT)
                .getIntentSender();
    }

    static IntentSender getAuthIntentSenderForDataset(Context context, String datasetName) {
        final Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(EXTRA_DATASET_NAME, datasetName);
        intent.putExtra(EXTRA_FOR_RESPONSE, false);
        return PendingIntent.getActivity(context, ++sDatasetPendingIntentId, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT)
                .getIntentSender();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.auth_activity);
        mCancel = (Button) findViewById(R.id.cancel);
        mLogin = (Button) findViewById(R.id.login);
        mMasterPassword = (EditText) findViewById(R.id.master_password);
        mLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }

        });

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onFailure();
                AuthActivity.this.finish();
            }
        });
    }

    private void login() {
        Editable password = mMasterPassword.getText();
        Log.d(TAG, "login: " + password);
        if (password.length() == 0) {
            onSuccess();
        } else {
            Toast.makeText(this, "Password incorrect", Toast.LENGTH_SHORT).show();
            onFailure();
        }
        finish();
    }

    @Override
    public void finish() {
        if (mReplyIntent != null) {
            setResult(RESULT_OK, mReplyIntent);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.finish();
    }

    private void onFailure() {
        Log.w(TAG, "Failed auth.");
        mReplyIntent = null;
    }

    private void onSuccess() {
        Intent intent = getIntent();
        boolean forResponse = intent.getBooleanExtra(EXTRA_FOR_RESPONSE, true);
        AssistStructure structure = intent.getParcelableExtra(EXTRA_ASSIST_STRUCTURE);
        StructureParser parser = new StructureParser(structure);
        parser.parse();
        AutoFillId usernameId = parser.getUsernameField().getId();
        AutoFillId passwordId = parser.getPasswordField().getId();
        Map<String, LoginCredential> loginCredentialMap =
                AutoFillData.getInstance().getCredentialsMap(this);
        if (usernameId == null || passwordId == null || loginCredentialMap == null ||
                loginCredentialMap.isEmpty()) {
            Log.d(TAG, "No Autofill data found for this Activity.");
            return;
        }
        mReplyIntent = new Intent();
        if (forResponse) {
            // The response protected by auth, so we can send the entire response since we
            // passed auth.
            FillResponse response = AutoFillHelper.newCredentialsResponse(this, false, usernameId,
                    passwordId, loginCredentialMap);
            if (response != null) {
                mReplyIntent.putExtra(EXTRA_AUTHENTICATION_RESULT, response);
            }
        } else {
            // The dataset selected by the user was protected by auth, so we can send that dataset.
            String datasetName = intent.getStringExtra(EXTRA_DATASET_NAME);
            if (loginCredentialMap.containsKey(datasetName)) {
                LoginCredential credential = loginCredentialMap.get(datasetName);
                Dataset dataset = AutoFillHelper
                        .newCredentialDataset(this, credential, usernameId, passwordId);
                mReplyIntent.putExtra(EXTRA_AUTHENTICATION_RESULT, dataset);
            }
        }
    }
}
