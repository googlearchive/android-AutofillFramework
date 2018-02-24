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

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class DatasetWithFilledAutofillFields {
    @Embedded
    public AutofillDataset autofillDataset;

    @Relation(parentColumn = "id", entityColumn = "datasetId", entity = FilledAutofillField.class)
    public List<FilledAutofillField> filledAutofillFields;

    public void add(FilledAutofillField filledAutofillField) {
        if (filledAutofillFields == null) {
            this.filledAutofillFields = new ArrayList<>();
        }
        this.filledAutofillFields.add(filledAutofillField);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatasetWithFilledAutofillFields that = (DatasetWithFilledAutofillFields) o;

        if (autofillDataset != null ? !autofillDataset.equals(that.autofillDataset) :
                that.autofillDataset != null)
            return false;
        return filledAutofillFields != null ?
                filledAutofillFields.equals(that.filledAutofillFields) :
                that.filledAutofillFields == null;
    }

    @Override
    public int hashCode() {
        int result = autofillDataset != null ? autofillDataset.hashCode() : 0;
        result = 31 * result + (filledAutofillFields != null ? filledAutofillFields.hashCode() : 0);
        return result;
    }
}
