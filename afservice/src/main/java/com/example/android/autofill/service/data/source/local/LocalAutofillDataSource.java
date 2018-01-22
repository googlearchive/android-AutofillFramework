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

import android.content.SharedPreferences;
import android.service.autofill.Dataset;

import com.example.android.autofill.service.data.DataCallback;
import com.example.android.autofill.service.data.source.AutofillDataSource;
import com.example.android.autofill.service.data.source.local.dao.AutofillDao;
import com.example.android.autofill.service.model.AutofillDataset;
import com.example.android.autofill.service.model.AutofillHint;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FieldType;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.model.FilledAutofillField;
import com.example.android.autofill.service.model.ResourceIdHeuristic;
import com.example.android.autofill.service.util.AppExecutors;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.android.autofill.service.util.Util.logw;

public class LocalAutofillDataSource implements AutofillDataSource {
    public static final String SHARED_PREF_KEY = "com.example.android.autofill"
            + ".service.datasource.LocalAutofillDataSource";
    private static final String DATASET_NUMBER_KEY = "datasetNumber";
    private static final Object sLock = new Object();

    private static LocalAutofillDataSource sInstance;

    private final AutofillDao mAutofillDao;
    private final SharedPreferences mSharedPreferences;
    private final AppExecutors mAppExecutors;

    private LocalAutofillDataSource(SharedPreferences sharedPreferences, AutofillDao autofillDao,
            AppExecutors appExecutors) {
        mSharedPreferences = sharedPreferences;
        mAutofillDao = autofillDao;
        mAppExecutors = appExecutors;
    }

    public static LocalAutofillDataSource getInstance(SharedPreferences sharedPreferences,
            AutofillDao autofillDao, AppExecutors appExecutors) {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new LocalAutofillDataSource(sharedPreferences, autofillDao,
                        appExecutors);
            }
            return sInstance;
        }
    }

    public static void clearInstance() {
        synchronized (sLock) {
            sInstance = null;
        }
    }

    @Override
    public void getAutofillDatasets(List<String> allAutofillHints,
            DataCallback<List<DatasetWithFilledAutofillFields>> datasetsCallback) {
        mAppExecutors.diskIO().execute(() -> {
            final List<String> typeNames = getFieldTypesForAutofillHints(allAutofillHints)
                    .stream()
                    .map(FieldTypeWithHeuristics::getFieldType)
                    .map(FieldType::getTypeName)
                    .collect(Collectors.toList());
            List<DatasetWithFilledAutofillFields> datasetsWithFilledAutofillFields =
                    mAutofillDao.getDatasets(typeNames);
            mAppExecutors.mainThread().execute(() ->
                    datasetsCallback.onLoaded(datasetsWithFilledAutofillFields)
            );
        });
    }

    @Override
    public void getAllAutofillDatasets(
            DataCallback<List<DatasetWithFilledAutofillFields>> datasetsCallback) {
        mAppExecutors.diskIO().execute(() -> {
            List<DatasetWithFilledAutofillFields> datasetsWithFilledAutofillFields =
                    mAutofillDao.getAllDatasets();
            mAppExecutors.mainThread().execute(() ->
                    datasetsCallback.onLoaded(datasetsWithFilledAutofillFields)
            );
        });
    }

    @Override
    public void getAutofillDataset(List<String> allAutofillHints, String datasetName,
            DataCallback<DatasetWithFilledAutofillFields> datasetsCallback) {
        mAppExecutors.diskIO().execute(() -> {
            // Room does not support TypeConverters for collections.
            List<DatasetWithFilledAutofillFields> autofillDatasetFields =
                    mAutofillDao.getDatasetsWithName(allAutofillHints, datasetName);
            if (autofillDatasetFields != null && !autofillDatasetFields.isEmpty()) {
                if (autofillDatasetFields.size() > 1) {
                    logw("More than 1 dataset with name %s", datasetName);
                }
                DatasetWithFilledAutofillFields dataset = autofillDatasetFields.get(0);

                mAppExecutors.mainThread().execute(() ->
                        datasetsCallback.onLoaded(dataset)
                );
            } else {
                mAppExecutors.mainThread().execute(() ->
                        datasetsCallback.onDataNotAvailable("No data found.")
                );
            }
        });
    }


    @Override
    public void saveAutofillDatasets(List<DatasetWithFilledAutofillFields>
            datasetsWithFilledAutofillFields) {
        mAppExecutors.diskIO().execute(() -> {
            for (DatasetWithFilledAutofillFields datasetWithFilledAutofillFields :
                    datasetsWithFilledAutofillFields) {
                List<FilledAutofillField> filledAutofillFields =
                        datasetWithFilledAutofillFields.filledAutofillFields;
                AutofillDataset autofillDataset = datasetWithFilledAutofillFields.autofillDataset;
                mAutofillDao.insertAutofillDataset(autofillDataset);
                mAutofillDao.insertFilledAutofillFields(filledAutofillFields);
            }
        });
        incrementDatasetNumber();
    }

    @Override
    public void saveResourceIdHeuristic(ResourceIdHeuristic resourceIdHeuristic) {
        mAppExecutors.diskIO().execute(() -> {
            mAutofillDao.insertResourceIdHeuristic(resourceIdHeuristic);
        });
    }

    @Override
    public void getFieldTypes(DataCallback<List<FieldTypeWithHeuristics>> fieldTypesCallback) {
        mAppExecutors.diskIO().execute(() -> {
            List<FieldTypeWithHeuristics> fieldTypeWithHints = mAutofillDao.getFieldTypesWithHints();
            mAppExecutors.mainThread().execute(() -> {
                if (fieldTypeWithHints != null) {
                    fieldTypesCallback.onLoaded(fieldTypeWithHints);
                } else {
                    fieldTypesCallback.onDataNotAvailable("Field Types not found.");
                }
            });
        });
    }

    @Override
    public void getFieldTypeByAutofillHints(
            DataCallback<HashMap<String, FieldTypeWithHeuristics>> fieldTypeMapCallback) {
        mAppExecutors.diskIO().execute(() -> {
            HashMap<String, FieldTypeWithHeuristics> hintMap = getFieldTypeByAutofillHints();
            mAppExecutors.mainThread().execute(() -> {
                if (hintMap != null) {
                    fieldTypeMapCallback.onLoaded(hintMap);
                } else {
                    fieldTypeMapCallback.onDataNotAvailable("FieldTypes not found");
                }
            });
        });
    }

    @Override
    public void getFilledAutofillField(String datasetId, String fieldTypeName, DataCallback<FilledAutofillField> fieldCallback) {
        mAppExecutors.diskIO().execute(() -> {
            FilledAutofillField filledAutofillField = mAutofillDao.getFilledAutofillField(datasetId, fieldTypeName);
            mAppExecutors.mainThread().execute(() -> {
                fieldCallback.onLoaded(filledAutofillField);
            });
        });
    }

    @Override
    public void getFieldType(String fieldTypeName, DataCallback<FieldType> fieldTypeCallback) {
        mAppExecutors.diskIO().execute(() -> {
            FieldType fieldType = mAutofillDao.getFieldType(fieldTypeName);
            mAppExecutors.mainThread().execute(() -> {
                fieldTypeCallback.onLoaded(fieldType);
            });
        });
    }

    public void getAutofillDatasetWithId(String datasetId,
            DataCallback<DatasetWithFilledAutofillFields> callback) {
        mAppExecutors.diskIO().execute(() -> {
            DatasetWithFilledAutofillFields dataset =
                    mAutofillDao.getAutofillDatasetWithId(datasetId);
            mAppExecutors.mainThread().execute(() -> {
                callback.onLoaded(dataset);
            });
        });
    }

    private HashMap<String, FieldTypeWithHeuristics> getFieldTypeByAutofillHints() {
        HashMap<String, FieldTypeWithHeuristics> hintMap = new HashMap<>();
        List<FieldTypeWithHeuristics> fieldTypeWithHints =
                mAutofillDao.getFieldTypesWithHints();
        if (fieldTypeWithHints != null) {
            for (FieldTypeWithHeuristics fieldType : fieldTypeWithHints) {
                for (AutofillHint hint : fieldType.autofillHints) {
                    hintMap.put(hint.mAutofillHint, fieldType);
                }
            }
            return hintMap;
        } else {
            return null;
        }
    }

    private List<FieldTypeWithHeuristics> getFieldTypesForAutofillHints(List<String> autofillHints) {
        return mAutofillDao.getFieldTypesForAutofillHints(autofillHints);
    }

    @Override
    public void clear() {
        mAppExecutors.diskIO().execute(() -> {
            mAutofillDao.clearAll();
            mSharedPreferences.edit().putInt(DATASET_NUMBER_KEY, 0).apply();
        });
    }

    /**
     * For simplicity, {@link Dataset}s will be named in the form {@code dataset-X.P} where
     * {@code X} means this was the Xth group of datasets saved, and {@code P} refers to the dataset
     * partition number. This method returns the appropriate {@code X}.
     */
    public int getDatasetNumber() {
        return mSharedPreferences.getInt(DATASET_NUMBER_KEY, 0);
    }

    /**
     * Every time a dataset is saved, this should be called to increment the dataset number.
     * (only important for this service's dataset naming scheme).
     */
    private void incrementDatasetNumber() {
        mSharedPreferences.edit().putInt(DATASET_NUMBER_KEY, getDatasetNumber() + 1).apply();
    }
}
