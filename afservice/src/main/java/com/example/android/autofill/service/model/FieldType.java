/*
 * Copyright (C) 2018 The Android Open Source Project
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
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import static com.example.android.autofill.service.data.source.local.db.Converters.IntList;

@Entity(primaryKeys = {"typeName"})
public class FieldType {
    @NonNull
    @ColumnInfo(name = "typeName")
    private final String mTypeName;

    @NonNull
    @ColumnInfo(name = "autofillTypes")
    private final IntList mAutofillTypes;

    @NonNull
    @ColumnInfo(name = "saveInfo")
    private final Integer mSaveInfo;

    @NonNull
    @ColumnInfo(name = "partition")
    private final Integer mPartition;

    @NonNull
    @Embedded
    private final FakeData mFakeData;

    public FieldType(@NonNull String typeName, @NonNull IntList autofillTypes,
            @NonNull Integer saveInfo, @NonNull Integer partition, @NonNull FakeData fakeData) {
        mTypeName = typeName;
        mAutofillTypes = autofillTypes;
        mSaveInfo = saveInfo;
        mPartition = partition;
        mFakeData = fakeData;
    }

    @NonNull
    public String getTypeName() {
        return mTypeName;
    }

    @NonNull
    public IntList getAutofillTypes() {
        return mAutofillTypes;
    }

    @NonNull
    public Integer getSaveInfo() {
        return mSaveInfo;
    }

    @NonNull
    public Integer getPartition() {
        return mPartition;
    }

    @NonNull
    public FakeData getFakeData() {
        return mFakeData;
    }
}