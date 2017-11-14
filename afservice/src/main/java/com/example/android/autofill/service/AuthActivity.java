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
package com.example.android.autofill.service;

import android.app.PendingIntent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.autofill.AutofillManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.autofill.service.datasource.SharedPrefsAutofillRepository;
import com.example.android.autofill.service.model.FilledAutofillFieldCollection;
import com.example.android.autofill.service.settings.MyPreferences;

import java.util.HashMap;

import static android.view.autofill.AutofillManager.EXTRA_ASSIST_STRUCTURE;
import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;
import static com.example.android.autofill.service.Util.EXTRA_DATASET_NAME;
import static com.example.android.autofill.service.Util.EXTRA_FOR_RESPONSE;
import static com.example.android.autofill.service.Util.logw;


/**
 * This Activity controls the UI for logging in to the Autofill service.
 * It is launched when an Autofill Response or specific Dataset within the Response requires
 * authentication to access. It bundles the result in an Intent.
 */
public class AuthActivity extends AppCompatActivity {

    // Unique id for dataset intents.
    private static int sDatasetPendingIntentId = 0;

    private EditText mMasterPassword;
    private Intent mReplyIntent;

    static IntentSender getAuthIntentSenderForResponse(Context context) {
        final Intent intent = new Intent(context, AuthActivity.class);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                .getIntentSender();
    }

    static IntentSender getAuthIntentSenderForDataset(Context context, String datasetName) {
        final Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(EXTRA_DATASET_NAME, datasetName);
        intent.putExtra(EXTRA_FOR_RESPONSE, false);
        return PendingIntent.getActivity(context, ++sDatasetPendingIntentId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT).getIntentSender();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multidataset_service_auth_activity);
        mMasterPassword = findViewById(R.id.master_password);
        findViewById(R.id.login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }

        });
        findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onFailure();
                AuthActivity.this.finish();
            }
        });
    }

    private void login() {
        Editable password = mMasterPassword.getText();
        if (password.toString()
                .equals(MyPreferences.getInstance(AuthActivity.this).getMasterPassword())) {
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
        logw("Failed auth.");
        mReplyIntent = null;
    }

    private void onSuccess() {
        Intent intent = getIntent();
        boolean forResponse = intent.getBooleanExtra(EXTRA_FOR_RESPONSE, true);
        Bundle clientState = intent.getBundleExtra(AutofillManager.EXTRA_CLIENT_STATE);
        AssistStructure structure = intent.getParcelableExtra(EXTRA_ASSIST_STRUCTURE);
        StructureParser parser = new StructureParser(getApplicationContext(), structure);
        parser.parseForFill();
        AutofillFieldMetadataCollection autofillFields = parser.getAutofillFields();
        int saveTypes = autofillFields.getSaveType();
        mReplyIntent = new Intent();
        HashMap<String, FilledAutofillFieldCollection> clientFormDataMap =
                SharedPrefsAutofillRepository.getInstance().getFilledAutofillFieldCollection
                        (this, autofillFields.getFocusedHints(), autofillFields.getAllHints());
        if (forResponse) {
            setResponseIntent(AutofillHelper.newResponse
                    (this, clientState, false, autofillFields, clientFormDataMap));
        } else {
            String datasetName = intent.getStringExtra(EXTRA_DATASET_NAME);
            setDatasetIntent(AutofillHelper.newDataset
                    (this, autofillFields, clientFormDataMap.get(datasetName), false));
        }
    }

    private void setResponseIntent(FillResponse fillResponse) {
        mReplyIntent.putExtra(EXTRA_AUTHENTICATION_RESULT, fillResponse);
    }

    private void setDatasetIntent(Dataset dataset) {
        mReplyIntent.putExtra(EXTRA_AUTHENTICATION_RESULT, dataset);
    }
}
