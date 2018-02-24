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

package com.example.android.autofill.service.model;

import static com.example.android.autofill.service.data.source.local.DigitalAssetLinksRepository.getCanonicalDomain;

public class DalInfo {
    private final String mWebDomain;
    private final String mPackageName;

    public DalInfo(String webDomain, String packageName) {
        String canonicalDomain = getCanonicalDomain(webDomain);
        final String fullDomain;
        if (!webDomain.startsWith("http:") && !webDomain.startsWith("https:")) {
            // Unfortunately AssistStructure.ViewNode does not tell what the domain is, so let's
            // assume it's https
            fullDomain = "https://" + canonicalDomain;
        } else {
            fullDomain = canonicalDomain;
        }
        mWebDomain = fullDomain;
        mPackageName = packageName;
    }

    public String getWebDomain() {
        return mWebDomain;
    }

    public String getPackageName() {
        return mPackageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DalInfo dalInfo = (DalInfo) o;

        if (mWebDomain != null ? !mWebDomain.equals(dalInfo.mWebDomain) :
                dalInfo.mWebDomain != null)
            return false;
        return mPackageName != null ? mPackageName.equals(dalInfo.mPackageName) :
                dalInfo.mPackageName == null;
    }

    @Override
    public int hashCode() {
        int result = mWebDomain != null ? mWebDomain.hashCode() : 0;
        result = 31 * result + (mPackageName != null ? mPackageName.hashCode() : 0);
        return result;
    }
}
