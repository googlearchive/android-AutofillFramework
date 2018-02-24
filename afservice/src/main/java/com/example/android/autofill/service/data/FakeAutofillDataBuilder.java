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

import com.example.android.autofill.service.AutofillHints;
import com.example.android.autofill.service.model.AutofillDataset;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.model.FilledAutofillField;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.UUID;

public class FakeAutofillDataBuilder implements AutofillDataBuilder {
    private final List<FieldTypeWithHeuristics> mFieldTypesWithHints;
    private final String mPackageName;
    private final int mSeed;

    public FakeAutofillDataBuilder(List<FieldTypeWithHeuristics> fieldTypesWithHints,
            String packageName, int seed) {
        mFieldTypesWithHints = fieldTypesWithHints;
        mSeed = seed;
        mPackageName = packageName;
    }

    @Override
    public List<DatasetWithFilledAutofillFields> buildDatasetsByPartition(int datasetNumber) {
        ImmutableList.Builder<DatasetWithFilledAutofillFields> listBuilder =
                new ImmutableList.Builder<>();
        for (int partition : AutofillHints.PARTITIONS) {
            AutofillDataset autofillDataset = new AutofillDataset(UUID.randomUUID().toString(),
                    "dataset-" + datasetNumber + "." + partition, mPackageName);
            DatasetWithFilledAutofillFields datasetWithFilledAutofillFields =
                    buildCollectionForPartition(autofillDataset, partition);
            if (datasetWithFilledAutofillFields != null &&
                    datasetWithFilledAutofillFields.filledAutofillFields != null &&
                    !datasetWithFilledAutofillFields.filledAutofillFields.isEmpty()) {
                listBuilder.add(datasetWithFilledAutofillFields);
            }
        }
        return listBuilder.build();
    }

    private DatasetWithFilledAutofillFields buildCollectionForPartition(
            AutofillDataset dataset, int partition) {
        DatasetWithFilledAutofillFields datasetWithFilledAutofillFields =
                new DatasetWithFilledAutofillFields();
        datasetWithFilledAutofillFields.autofillDataset = dataset;
        for (FieldTypeWithHeuristics fieldTypeWithHeuristics : mFieldTypesWithHints) {
            if (AutofillHints.matchesPartition(
                    fieldTypeWithHeuristics.getFieldType().getPartition(), partition)) {
                FilledAutofillField fakeField =
                        AutofillHints.generateFakeField(fieldTypeWithHeuristics, mPackageName,
                                mSeed, dataset.getId());
                datasetWithFilledAutofillFields.add(fakeField);
            }
        }
        return datasetWithFilledAutofillFields;
    }
}
