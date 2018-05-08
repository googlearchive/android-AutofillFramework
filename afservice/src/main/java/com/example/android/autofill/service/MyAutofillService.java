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

import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.support.annotation.NonNull;
import android.view.autofill.AutofillManager;
import android.widget.RemoteViews;

import com.example.android.autofill.service.data.AutofillDataBuilder;
import com.example.android.autofill.service.data.ClientAutofillDataBuilder;
import com.example.android.autofill.service.data.ClientViewMetadata;
import com.example.android.autofill.service.data.ClientViewMetadataBuilder;
import com.example.android.autofill.service.data.DataCallback;
import com.example.android.autofill.service.data.adapter.DatasetAdapter;
import com.example.android.autofill.service.data.adapter.ResponseAdapter;
import com.example.android.autofill.service.data.source.DefaultFieldTypesSource;
import com.example.android.autofill.service.data.source.PackageVerificationDataSource;
import com.example.android.autofill.service.data.source.local.DefaultFieldTypesLocalJsonSource;
import com.example.android.autofill.service.data.source.local.DigitalAssetLinksRepository;
import com.example.android.autofill.service.data.source.local.LocalAutofillDataSource;
import com.example.android.autofill.service.data.source.local.SharedPrefsPackageVerificationRepository;
import com.example.android.autofill.service.data.source.local.dao.AutofillDao;
import com.example.android.autofill.service.data.source.local.db.AutofillDatabase;
import com.example.android.autofill.service.model.DalCheck;
import com.example.android.autofill.service.model.DalInfo;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.settings.MyPreferences;
import com.example.android.autofill.service.util.AppExecutors;
import com.example.android.autofill.service.util.Util;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.android.autofill.service.util.Util.DalCheckRequirement;
import static com.example.android.autofill.service.util.Util.bundleToString;
import static com.example.android.autofill.service.util.Util.dumpStructure;
import static com.example.android.autofill.service.util.Util.logVerboseEnabled;
import static com.example.android.autofill.service.util.Util.logd;
import static com.example.android.autofill.service.util.Util.loge;
import static com.example.android.autofill.service.util.Util.logv;
import static com.example.android.autofill.service.util.Util.logw;
import static java.util.stream.Collectors.toList;

public class MyAutofillService extends AutofillService {

    private LocalAutofillDataSource mLocalAutofillDataSource;
    private DigitalAssetLinksRepository mDalRepository;
    private PackageVerificationDataSource mPackageVerificationRepository;
    private AutofillDataBuilder mAutofillDataBuilder;
    private ResponseAdapter mResponseAdapter;
    private ClientViewMetadata mClientViewMetadata;
    private MyPreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = MyPreferences.getInstance(this);
        Util.setLoggingLevel(mPreferences.getLoggingLevel());
        SharedPreferences localAfDataSourceSharedPrefs =
                getSharedPreferences(LocalAutofillDataSource.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        DefaultFieldTypesSource defaultFieldTypesSource =
                DefaultFieldTypesLocalJsonSource.getInstance(getResources(),
                        new GsonBuilder().create());
        AutofillDao autofillDao = AutofillDatabase.getInstance(this,
                defaultFieldTypesSource, new AppExecutors()).autofillDao();
        mLocalAutofillDataSource = LocalAutofillDataSource.getInstance(localAfDataSourceSharedPrefs,
                autofillDao, new AppExecutors());
        mDalRepository = DigitalAssetLinksRepository.getInstance(getPackageManager());
        mPackageVerificationRepository = SharedPrefsPackageVerificationRepository.getInstance(this);
    }

    @Override
    public void onFillRequest(@NonNull FillRequest request,
            @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
        List<FillContext> fillContexts = request.getFillContexts();
        List<AssistStructure> structures =
                fillContexts.stream().map(FillContext::getStructure).collect(toList());
        AssistStructure latestStructure = fillContexts.get(fillContexts.size() - 1).getStructure();
        ClientParser parser = new ClientParser(structures);

        // Check user's settings for authenticating Responses and Datasets.
        boolean responseAuth = mPreferences.isResponseAuth();
        boolean datasetAuth = mPreferences.isDatasetAuth();
        boolean manual = (request.getFlags() & FillRequest.FLAG_MANUAL_REQUEST) != 0;
        mLocalAutofillDataSource.getFieldTypeByAutofillHints(
                new DataCallback<HashMap<String, FieldTypeWithHeuristics>>() {
                    @Override
                    public void onLoaded(HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint) {
                        DatasetAdapter datasetAdapter = new DatasetAdapter(parser);
                        ClientViewMetadataBuilder clientViewMetadataBuilder =
                                new ClientViewMetadataBuilder(parser, fieldTypesByAutofillHint);
                        mClientViewMetadata = clientViewMetadataBuilder.buildClientViewMetadata();
                        mResponseAdapter = new ResponseAdapter(MyAutofillService.this,
                                mClientViewMetadata, getPackageName(), datasetAdapter);
                        String packageName = latestStructure.getActivityComponent().getPackageName();
                        if (!mPackageVerificationRepository.putPackageSignatures(packageName)) {
                            callback.onFailure(getString(R.string.invalid_package_signature));
                            return;
                        }
                        if (logVerboseEnabled()) {
                            logv("onFillRequest(): clientState=%s",
                                    bundleToString(request.getClientState()));
                            dumpStructure(latestStructure);
                        }
                        cancellationSignal.setOnCancelListener(() ->
                                logw("Cancel autofill not implemented in this sample.")
                        );
                        fetchDataAndGenerateResponse(fieldTypesByAutofillHint, responseAuth,
                                datasetAuth, manual, callback);
                    }

                    @Override
                    public void onDataNotAvailable(String msg, Object... params) {

                    }
                });
    }

    private void fetchDataAndGenerateResponse(
            HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint, boolean responseAuth,
            boolean datasetAuth, boolean manual, FillCallback callback) {
        if (responseAuth) {
            // If the entire Autofill Response is authenticated, AuthActivity is used
            // to generate Response.
            IntentSender sender = AuthActivity.getAuthIntentSenderForResponse(this);
            RemoteViews remoteViews = RemoteViewsHelper.viewsWithAuth(getPackageName(),
                    getString(R.string.autofill_sign_in_prompt));
            FillResponse response = mResponseAdapter.buildResponse(sender, remoteViews);
            if (response != null) {
                callback.onSuccess(response);
            }
        } else {
            mLocalAutofillDataSource.getAutofillDatasets(mClientViewMetadata.getAllHints(),
                    new DataCallback<List<DatasetWithFilledAutofillFields>>() {
                        @Override
                        public void onLoaded(List<DatasetWithFilledAutofillFields> datasets) {
                            if ((datasets == null || datasets.isEmpty()) && manual) {
                                IntentSender sender = ManualActivity
                                        .getManualIntentSenderForResponse(MyAutofillService.this);
                                RemoteViews remoteViews = RemoteViewsHelper.viewsWithNoAuth(
                                        getPackageName(),
                                        getString(R.string.autofill_manual_prompt));
                                FillResponse response = mResponseAdapter.buildManualResponse(sender,
                                        remoteViews);
                                if (response != null) {
                                    callback.onSuccess(response);
                                }
                            } else {
                                FillResponse response = mResponseAdapter.buildResponse(
                                        fieldTypesByAutofillHint, datasets, datasetAuth);
                                callback.onSuccess(response);
                            }
                        }

                        @Override
                        public void onDataNotAvailable(String msg, Object... params) {
                            logw(msg, params);
                            callback.onFailure(String.format(msg, params));
                        }
                    });
        }
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
        List<FillContext> fillContexts = request.getFillContexts();
        List<AssistStructure> structures =
                fillContexts.stream().map(FillContext::getStructure).collect(toList());
        AssistStructure latestStructure = fillContexts.get(fillContexts.size() - 1).getStructure();
        ClientParser parser = new ClientParser(structures);
        mLocalAutofillDataSource.getFieldTypeByAutofillHints(
                new DataCallback<HashMap<String, FieldTypeWithHeuristics>>() {
                    @Override
                    public void onLoaded(
                            HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint) {
                        mAutofillDataBuilder = new ClientAutofillDataBuilder(
                                fieldTypesByAutofillHint, getPackageName(), parser);
                        ClientViewMetadataBuilder clientViewMetadataBuilder =
                                new ClientViewMetadataBuilder(parser, fieldTypesByAutofillHint);
                        mClientViewMetadata = clientViewMetadataBuilder.buildClientViewMetadata();
                        String packageName = latestStructure.getActivityComponent().getPackageName();
                        if (!mPackageVerificationRepository.putPackageSignatures(packageName)) {
                            callback.onFailure(getString(R.string.invalid_package_signature));
                            return;
                        }
                        if (logVerboseEnabled()) {
                            logv("onSaveRequest(): clientState=%s",
                                    bundleToString(request.getClientState()));
                        }
                        dumpStructure(latestStructure);
                        checkWebDomainAndBuildAutofillData(packageName, callback);
                    }

                    @Override
                    public void onDataNotAvailable(String msg, Object... params) {
                        loge("Should not happen - could not find field types.");
                    }
                });
    }

    private void checkWebDomainAndBuildAutofillData(String packageName, SaveCallback callback) {
        String webDomain;
        try {
            webDomain = mClientViewMetadata.getWebDomain();
        } catch (SecurityException e) {
            logw(e.getMessage());
            callback.onFailure(getString(R.string.security_exception));
            return;
        }
        if (webDomain != null && webDomain.length() > 0) {
            DalCheckRequirement req = mPreferences.getDalCheckRequirement();
            mDalRepository.checkValid(req, new DalInfo(webDomain, packageName),
                    new DataCallback<DalCheck>() {
                        @Override
                        public void onLoaded(DalCheck dalCheck) {
                            if (dalCheck.linked) {
                                logd("Domain %s is valid for %s", webDomain, packageName);
                                buildAndSaveAutofillData();
                            } else {
                                loge("Could not associate web domain %s with app %s",
                                        webDomain, packageName);
                                callback.onFailure(getString(R.string.dal_exception));
                            }
                        }

                        @Override
                        public void onDataNotAvailable(String msg, Object... params) {
                            logw(msg, params);
                            callback.onFailure(getString(R.string.dal_exception));
                        }
                    });
        } else {
            logd("no web domain");
            buildAndSaveAutofillData();
        }
    }

    private void buildAndSaveAutofillData() {
        int datasetNumber = mLocalAutofillDataSource.getDatasetNumber();
        List<DatasetWithFilledAutofillFields> datasetsWithFilledAutofillFields =
                mAutofillDataBuilder.buildDatasetsByPartition(datasetNumber);
        mLocalAutofillDataSource.saveAutofillDatasets(datasetsWithFilledAutofillFields);
    }

    @Override
    public void onConnected() {
        logd("onConnected");
    }

    @Override
    public void onDisconnected() {
        logd("onDisconnected");
    }
}
