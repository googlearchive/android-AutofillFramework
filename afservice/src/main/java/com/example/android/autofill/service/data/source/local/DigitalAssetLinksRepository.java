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
package com.example.android.autofill.service.data.source.local;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.example.android.autofill.service.data.DataCallback;
import com.example.android.autofill.service.data.source.DalService;
import com.example.android.autofill.service.data.source.DigitalAssetLinksDataSource;
import com.example.android.autofill.service.model.DalCheck;
import com.example.android.autofill.service.model.DalInfo;
import com.example.android.autofill.service.util.SecurityHelper;
import com.google.common.net.InternetDomainName;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.android.autofill.service.util.Util.DalCheckRequirement;
import static com.example.android.autofill.service.util.Util.DalCheckRequirement.AllUrls;
import static com.example.android.autofill.service.util.Util.DalCheckRequirement.Disabled;
import static com.example.android.autofill.service.util.Util.DalCheckRequirement.LoginOnly;
import static com.example.android.autofill.service.util.Util.logd;


/**
 * Singleton repository that caches the result of Digital Asset Links checks.
 */
public class DigitalAssetLinksRepository implements DigitalAssetLinksDataSource {
    private static final String DAL_BASE_URL = "https://digitalassetlinks.googleapis.com";
    private static final String PERMISSION_GET_LOGIN_CREDS = "common.get_login_creds";
    private static final String PERMISSION_HANDLE_ALL_URLS = "common.handle_all_urls";
    private static DigitalAssetLinksRepository sInstance;

    private final PackageManager mPackageManager;
    private final DalService mDalService;
    private final HashMap<DalInfo, DalCheck> mCache;

    private DigitalAssetLinksRepository(PackageManager packageManager) {
        mPackageManager = packageManager;
        mCache = new HashMap<>();
        mDalService = new Retrofit.Builder()
                .baseUrl(DAL_BASE_URL)
                .build()
                .create(DalService.class);
    }

    public static DigitalAssetLinksRepository getInstance(PackageManager packageManager) {
        if (sInstance == null) {
            sInstance = new DigitalAssetLinksRepository(packageManager);
        }
        return sInstance;
    }

    public static String getCanonicalDomain(String domain) {
        InternetDomainName idn = InternetDomainName.from(domain);
        while (idn != null && !idn.isTopPrivateDomain()) {
            idn = idn.parent();
        }
        return idn == null ? null : idn.toString();
    }

    @Override
    public void clear() {
        mCache.clear();
    }

    public void checkValid(DalCheckRequirement dalCheckRequirement, DalInfo dalInfo,
            DataCallback<DalCheck> dalCheckDataCallback) {
        if (dalCheckRequirement.equals(Disabled)) {
            DalCheck dalCheck = new DalCheck();
            dalCheck.linked = true;
            dalCheckDataCallback.onLoaded(dalCheck);
            return;
        }

        DalCheck dalCheck = mCache.get(dalInfo);
        if (dalCheck != null) {
            dalCheckDataCallback.onLoaded(dalCheck);
            return;
        }
        String packageName = dalInfo.getPackageName();
        String webDomain = dalInfo.getWebDomain();

        final String fingerprint;
        try {
            PackageInfo packageInfo = mPackageManager.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
            fingerprint = SecurityHelper.getFingerprint(packageInfo, packageName);
        } catch (Exception e) {
            dalCheckDataCallback.onDataNotAvailable("Error getting fingerprint for %s",
                    packageName);
            return;
        }
        logd("validating domain %s for pkg %s and fingerprint %s.", webDomain,
                packageName, fingerprint);
        mDalService.check(webDomain, PERMISSION_GET_LOGIN_CREDS, packageName, fingerprint).enqueue(
                new Callback<DalCheck>() {
                    @Override
                    public void onResponse(@NonNull Call<DalCheck> call,
                            @NonNull Response<DalCheck> response) {
                        DalCheck dalCheck = response.body();
                        if (dalCheck == null || !dalCheck.linked) {
                            // get_login_creds check failed, so try handle_all_urls check
                            if (dalCheckRequirement.equals(LoginOnly)) {
                                dalCheckDataCallback.onDataNotAvailable(
                                        "DAL: Login creds check failed.");
                            } else if (dalCheckRequirement.equals(AllUrls)) {
                                mDalService.check(webDomain, PERMISSION_HANDLE_ALL_URLS,
                                        packageName, fingerprint).enqueue(new Callback<DalCheck>() {
                                    @Override
                                    public void onResponse(@NonNull Call<DalCheck> call,
                                            @NonNull Response<DalCheck> response) {
                                        DalCheck dalCheck = response.body();
                                        mCache.put(dalInfo, dalCheck);
                                        dalCheckDataCallback.onLoaded(dalCheck);
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<DalCheck> call,
                                            @NonNull Throwable t) {
                                        dalCheckDataCallback.onDataNotAvailable(t.getMessage());
                                    }
                                });
                            }
                        } else {
                            // get_login_creds check succeeded, so we're finished.
                            mCache.put(dalInfo, dalCheck);
                            dalCheckDataCallback.onLoaded(dalCheck);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DalCheck> call, @NonNull Throwable t) {
                        // get_login_creds check failed, so try handle_all_urls check.
                        mDalService.check(webDomain, PERMISSION_HANDLE_ALL_URLS, packageName,
                                fingerprint);
                    }
                });
    }
}
