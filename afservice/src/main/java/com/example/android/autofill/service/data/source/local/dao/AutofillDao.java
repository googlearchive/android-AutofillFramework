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

package com.example.android.autofill.service.data.source.local.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.android.autofill.service.model.AutofillDataset;
import com.example.android.autofill.service.model.AutofillHint;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FieldType;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.model.FilledAutofillField;
import com.example.android.autofill.service.model.ResourceIdHeuristic;

import java.util.Collection;
import java.util.List;

@Dao
public interface AutofillDao {
    /**
     * Fetches a list of datasets associated to autofill fields on the page.
     *
     * @param allAutofillHints Filtering parameter; represents all of the hints associated with
     *                         all of the views on the page.
     */
    @Query("SELECT DISTINCT id, datasetName FROM FilledAutofillField, AutofillDataset" +
            " WHERE AutofillDataset.id = FilledAutofillField.datasetId" +
            " AND FilledAutofillField.fieldTypeName IN (:allAutofillHints)")
    List<DatasetWithFilledAutofillFields> getDatasets(List<String> allAutofillHints);

    @Query("SELECT DISTINCT id, datasetName FROM FilledAutofillField, AutofillDataset" +
            " WHERE AutofillDataset.id = FilledAutofillField.datasetId")
    List<DatasetWithFilledAutofillFields> getAllDatasets();

    /**
     * Fetches a list of datasets associated to autofill fields. It should only return a dataset
     * if that dataset has an autofill field associate with the view the user is focused on, and
     * if that dataset's name matches the name passed in.
     *
     * @param fieldTypes Filtering parameter; represents all of the field types associated with
     *                         all of the views on the page.
     * @param datasetName      Filtering parameter; only return datasets with this name.
     */
    @Query("SELECT DISTINCT id, datasetName FROM FilledAutofillField, AutofillDataset" +
            " WHERE AutofillDataset.id = FilledAutofillField.datasetId" +
            " AND AutofillDataset.datasetName = (:datasetName)" +
            " AND FilledAutofillField.fieldTypeName IN (:fieldTypes)")
    List<DatasetWithFilledAutofillFields> getDatasetsWithName(
            List<String> fieldTypes, String datasetName);

    @Query("SELECT DISTINCT typeName, autofillTypes, saveInfo, partition, strictExampleSet, " +
            "textTemplate, dateTemplate" +
            " FROM FieldType, AutofillHint" +
            " WHERE FieldType.typeName = AutofillHint.fieldTypeName" +
            " UNION " +
            "SELECT DISTINCT typeName, autofillTypes, saveInfo, partition, strictExampleSet, " +
            "textTemplate, dateTemplate" +
            " FROM FieldType, ResourceIdHeuristic" +
            " WHERE FieldType.typeName = ResourceIdHeuristic.fieldTypeName")
    List<FieldTypeWithHeuristics> getFieldTypesWithHints();

    @Query("SELECT DISTINCT typeName, autofillTypes, saveInfo, partition, strictExampleSet, " +
            "textTemplate, dateTemplate" +
            " FROM FieldType, AutofillHint" +
            " WHERE FieldType.typeName = AutofillHint.fieldTypeName" +
            " AND AutofillHint.autofillHint IN (:autofillHints)" +
            " UNION " +
            "SELECT DISTINCT typeName, autofillTypes, saveInfo, partition, strictExampleSet, " +
            "textTemplate, dateTemplate" +
            " FROM FieldType, ResourceIdHeuristic" +
            " WHERE FieldType.typeName = ResourceIdHeuristic.fieldTypeName")
    List<FieldTypeWithHeuristics> getFieldTypesForAutofillHints(List<String> autofillHints);

    @Query("SELECT DISTINCT id, datasetName FROM FilledAutofillField, AutofillDataset" +
            " WHERE AutofillDataset.id = FilledAutofillField.datasetId" +
            " AND AutofillDataset.id = (:datasetId)")
    DatasetWithFilledAutofillFields getAutofillDatasetWithId(String datasetId);

    @Query("SELECT * FROM FilledAutofillField" +
            " WHERE FilledAutofillField.datasetId = (:datasetId)" +
            " AND FilledAutofillField.fieldTypeName = (:fieldTypeName)")
    FilledAutofillField getFilledAutofillField(String datasetId, String fieldTypeName);

    @Query("SELECT * FROM FieldType" +
            " WHERE FieldType.typeName = (:fieldTypeName)")
    FieldType getFieldType(String fieldTypeName);

    /**
     * @param autofillFields Collection of autofill fields to be saved to the db.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFilledAutofillFields(Collection<FilledAutofillField> autofillFields);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAutofillDataset(AutofillDataset datasets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAutofillHints(List<AutofillHint> autofillHints);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertResourceIdHeuristic(ResourceIdHeuristic resourceIdHeuristic);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFieldTypes(List<FieldType> fieldTypes);


    @Query("DELETE FROM AutofillDataset")
    void clearAll();
}