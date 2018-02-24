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

package com.example.android.autofill.service.data.source.local;

import android.content.res.Resources;

import com.example.android.autofill.service.R;
import com.example.android.autofill.service.data.source.DefaultFieldTypesSource;
import com.example.android.autofill.service.model.DefaultFieldTypeWithHints;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

import static com.example.android.autofill.service.util.Util.loge;

public class DefaultFieldTypesLocalJsonSource implements DefaultFieldTypesSource {
    private static DefaultFieldTypesLocalJsonSource sInstance;

    private final Resources mResources;
    private final Gson mGson;

    private DefaultFieldTypesLocalJsonSource(Resources resources, Gson gson) {
        mResources = resources;
        mGson = gson;
    }

    public static DefaultFieldTypesLocalJsonSource getInstance(Resources resources, Gson gson) {
        if (sInstance == null) {
            sInstance = new DefaultFieldTypesLocalJsonSource(resources, gson);
        }
        return sInstance;
    }

    @Override
    public List<DefaultFieldTypeWithHints> getDefaultFieldTypes() {
        Type fieldTypeListType =  TypeToken.getParameterized(List.class,
                DefaultFieldTypeWithHints.class).getType();
        InputStream is = mResources.openRawResource(R.raw.default_field_types);
        List<DefaultFieldTypeWithHints> fieldTypes = null;
        try(Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            fieldTypes = mGson.fromJson(reader, fieldTypeListType);
        } catch (IOException e) {
            loge(e, "Exception during deserialization of FieldTypes.");
        }
        return fieldTypes;
    }
}
