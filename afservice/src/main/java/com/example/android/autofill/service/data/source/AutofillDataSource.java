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
package com.example.android.autofill.service.data.source;

import com.example.android.autofill.service.data.DataCallback;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FieldType;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.model.FilledAutofillField;
import com.example.android.autofill.service.model.ResourceIdHeuristic;

import java.util.HashMap;
import java.util.List;

public interface AutofillDataSource {

    /**
     * Asynchronously gets saved list of {@link DatasetWithFilledAutofillFields} that contains some
     * objects that can autofill fields with these {@code autofillHints}.
     */
    void getAutofillDatasets(List<String> allAutofillHints,
            DataCallback<List<DatasetWithFilledAutofillFields>> datasetsCallback);

    void getAllAutofillDatasets(
            DataCallback<List<DatasetWithFilledAutofillFields>> datasetsCallback);

    /**
     * Asynchronously gets a saved {@link DatasetWithFilledAutofillFields} for a specific
     * {@code datasetName} that contains some objects that can autofill fields with these
     * {@code autofillHints}.
     */
    void getAutofillDataset(List<String> allAutofillHints,
            String datasetName, DataCallback<DatasetWithFilledAutofillFields> datasetsCallback);

    /**
     * Stores a collection of Autofill fields.
     */
    void saveAutofillDatasets(List<DatasetWithFilledAutofillFields>
            datasetsWithFilledAutofillFields);

    void saveResourceIdHeuristic(ResourceIdHeuristic resourceIdHeuristic);

    /**
     * Gets all autofill field types.
     */
    void getFieldTypes(DataCallback<List<FieldTypeWithHeuristics>> fieldTypesCallback);

    /**
     * Gets all autofill field types.
     */
    void getFieldType(String typeName, DataCallback<FieldType> fieldTypeCallback);

    void getFieldTypeByAutofillHints(
            DataCallback<HashMap<String, FieldTypeWithHeuristics>> fieldTypeMapCallback);

    void getFilledAutofillField(String datasetId, String fieldTypeName, DataCallback<FilledAutofillField> fieldCallback);

    /**
     * Clears all data.
     */
    void clear();
}
