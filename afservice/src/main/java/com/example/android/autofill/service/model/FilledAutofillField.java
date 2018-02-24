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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import javax.annotation.Nullable;

@Entity(primaryKeys = {"datasetId", "fieldTypeName"}, foreignKeys = {
        @ForeignKey(entity = AutofillDataset.class, parentColumns = "id",
                childColumns = "datasetId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = FieldType.class, parentColumns = "typeName",
                childColumns = "fieldTypeName", onDelete = ForeignKey.CASCADE)
})
public class FilledAutofillField {

    @NonNull
    @ColumnInfo(name = "datasetId")
    private final String mDatasetId;

    @Nullable
    @ColumnInfo(name = "textValue")
    private final String mTextValue;

    @Nullable
    @ColumnInfo(name = "dateValue")
    private final Long mDateValue;

    @Nullable
    @ColumnInfo(name = "toggleValue")
    private final Boolean mToggleValue;

    @NonNull
    @ColumnInfo(name = "fieldTypeName")
    private final String mFieldTypeName;

    public FilledAutofillField(@NonNull String datasetId, @NonNull String fieldTypeName,
                               @Nullable String textValue, @Nullable Long dateValue,
                               @Nullable Boolean toggleValue) {
        mDatasetId = datasetId;
        mFieldTypeName = fieldTypeName;
        mTextValue = textValue;
        mDateValue = dateValue;
        mToggleValue = toggleValue;
    }

    @Ignore
    public FilledAutofillField(@NonNull String datasetId,
            @NonNull String fieldTypeName, @Nullable String textValue, @Nullable Long dateValue) {
        this(datasetId, fieldTypeName, textValue, dateValue, null);
    }

    @Ignore
    public FilledAutofillField(@NonNull String datasetId, @NonNull String fieldTypeName,
                               @Nullable String textValue) {
        this(datasetId, fieldTypeName, textValue, null, null);
    }

    @Ignore
    public FilledAutofillField(@NonNull String datasetId, @NonNull String fieldTypeName,
                               @Nullable Long dateValue) {
        this(datasetId, fieldTypeName, null, dateValue, null);
    }

    @Ignore
    public FilledAutofillField(@NonNull String datasetId, @NonNull String fieldTypeName,
                               @Nullable Boolean toggleValue) {
        this(datasetId, fieldTypeName, null, null, toggleValue);
    }

    @Ignore
    public FilledAutofillField(@NonNull String datasetId, @NonNull String fieldTypeName) {
        this(datasetId, fieldTypeName, null, null, null);
    }

    @NonNull
    public String getDatasetId() {
        return mDatasetId;
    }

    @Nullable
    public String getTextValue() {
        return mTextValue;
    }

    @Nullable
    public Long getDateValue() {
        return mDateValue;
    }

    @Nullable
    public Boolean getToggleValue() {
        return mToggleValue;
    }

    @NonNull
    public String getFieldTypeName() {
        return mFieldTypeName;
    }

    public boolean isNull() {
        return mTextValue == null && mDateValue == null && mToggleValue == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilledAutofillField that = (FilledAutofillField) o;

        if (mTextValue != null ? !mTextValue.equals(that.mTextValue) : that.mTextValue != null)
            return false;
        if (mDateValue != null ? !mDateValue.equals(that.mDateValue) : that.mDateValue != null)
            return false;
        if (mToggleValue != null ? !mToggleValue.equals(that.mToggleValue) : that.mToggleValue != null)
            return false;
        return mFieldTypeName.equals(that.mFieldTypeName);
    }

    @Override
    public int hashCode() {
        int result = mTextValue != null ? mTextValue.hashCode() : 0;
        result = 31 * result + (mDateValue != null ? mDateValue.hashCode() : 0);
        result = 31 * result + (mToggleValue != null ? mToggleValue.hashCode() : 0);
        result = 31 * result + mFieldTypeName.hashCode();
        return result;
    }
}
