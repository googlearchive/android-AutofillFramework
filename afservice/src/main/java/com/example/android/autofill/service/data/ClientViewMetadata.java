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

package com.example.android.autofill.service.data;

import android.service.autofill.SaveInfo;
import android.view.autofill.AutofillId;

import java.util.Arrays;
import java.util.List;

/**
 * In this simple implementation, the only view data we collect from the client are autofill hints
 * of the views in the view hierarchy, the corresponding autofill IDs, and the {@link SaveInfo}
 * based on the hints.
 */
public class ClientViewMetadata {
    private final List<String> mAllHints;
    private final int mSaveType;
    private final AutofillId[] mAutofillIds;
    private final String mWebDomain;
    private final AutofillId[] mFocusedIds;

    public ClientViewMetadata(List<String> allHints, int saveType, AutofillId[] autofillIds,
            AutofillId[] focusedIds, String webDomain) {
        mAllHints = allHints;
        mSaveType = saveType;
        mAutofillIds = autofillIds;
        mWebDomain = webDomain;
        mFocusedIds = focusedIds;
    }

    public List<String> getAllHints() {
        return mAllHints;
    }

    public AutofillId[] getAutofillIds() {
        return mAutofillIds;
    }

    public AutofillId[] getFocusedIds() {
        return mFocusedIds;
    }

    public int getSaveType() {
        return mSaveType;
    }

    public String getWebDomain() {
        return mWebDomain;
    }

    @Override public String toString() {
        return "ClientViewMetadata{" +
                "mAllHints=" + mAllHints +
                ", mSaveType=" + mSaveType +
                ", mAutofillIds=" + Arrays.toString(mAutofillIds) +
                ", mWebDomain='" + mWebDomain + '\'' +
                ", mFocusedIds=" + Arrays.toString(mFocusedIds) +
                '}';
    }
}
