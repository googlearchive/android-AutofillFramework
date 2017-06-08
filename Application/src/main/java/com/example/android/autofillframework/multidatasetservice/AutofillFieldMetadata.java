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
package com.example.android.autofillframework.multidatasetservice;

import android.app.assist.AssistStructure.ViewNode;
import android.service.autofill.SaveInfo;
import android.view.View;
import android.view.autofill.AutofillId;

/**
 * A stripped down version of a {@link ViewNode} that contains only autofill-relevant metadata. It
 * also contains a {@code mSaveType} flag that is calculated based on the {@link ViewNode}]'s
 * autofill hints.
 */
public class AutofillFieldMetadata {
    private int mSaveType = 0;
    private String[] mAutofillHints;
    private AutofillId mAutofillId;
    private int mAutofillType;
    private CharSequence[] mAutofillOptions;
    private boolean mFocused;

    public AutofillFieldMetadata(ViewNode view) {
        mAutofillId = view.getAutofillId();
        mAutofillType = view.getAutofillType();
        mAutofillOptions = view.getAutofillOptions();
        mFocused = view.isFocused();
        setHints(view.getAutofillHints());
    }

    public String[] getHints() {
        return mAutofillHints;
    }

    public void setHints(String[] hints) {
        mAutofillHints = hints;
        updateSaveTypeFromHints();
    }

    public int getSaveType() {
        return mSaveType;
    }

    public AutofillId getId() {
        return mAutofillId;
    }

    public int getAutofillType() {
        return mAutofillType;
    }

    /**
     * When the {@link ViewNode} is a list that the user needs to choose a string from (i.e. a
     * spinner), this is called to return the index of a specific item in the list.
     */
    public int getAutofillOptionIndex(String value) {
        for (int i = 0; i < mAutofillOptions.length; i++) {
            if (mAutofillOptions[i].toString().equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isFocused() {
        return mFocused;
    }

    private void updateSaveTypeFromHints() {
        mSaveType = 0;
        if (mAutofillHints == null) {
            return;
        }
        for (String hint : mAutofillHints) {
            switch (hint) {
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
                case View.AUTOFILL_HINT_CREDIT_CARD_NUMBER:
                case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_CREDIT_CARD;
                    break;
                case View.AUTOFILL_HINT_EMAIL_ADDRESS:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS;
                    break;
                case View.AUTOFILL_HINT_PHONE:
                case View.AUTOFILL_HINT_NAME:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_GENERIC;
                    break;
                case View.AUTOFILL_HINT_PASSWORD:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_PASSWORD;
                    mSaveType &= ~SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS;
                    mSaveType &= ~SaveInfo.SAVE_DATA_TYPE_USERNAME;
                    break;
                case View.AUTOFILL_HINT_POSTAL_ADDRESS:
                case View.AUTOFILL_HINT_POSTAL_CODE:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_ADDRESS;
                    break;
                case View.AUTOFILL_HINT_USERNAME:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_USERNAME;
                    break;
            }
        }
    }
}
