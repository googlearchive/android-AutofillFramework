/*
 * Copyright 2017, The Android Open Source Project
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

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.example.android.autofill.service.data.source.local.db.AutofillDatabase;
import com.example.android.autofill.service.model.AutofillDataset;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FilledAutofillField;
import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

@RunWith(AndroidJUnit4.class)
public class AutofillDaoTest {
    private final AutofillDataset mDataset =
            new AutofillDataset(UUID.randomUUID().toString(),
                    "dataset-1", InstrumentationRegistry.getContext().getPackageName());
    private final FilledAutofillField mUsernameField =
            new FilledAutofillField(mDataset.getId(), View.AUTOFILL_HINT_USERNAME, "login");
    private final FilledAutofillField mPasswordField =
            new FilledAutofillField(mDataset.getId(), View.AUTOFILL_HINT_PASSWORD, "password");

    private AutofillDatabase mDatabase;

    @Before
    public void setup() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                AutofillDatabase.class).build();

    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertFilledAutofillFieldAndGet() {
        DatasetWithFilledAutofillFields datasetWithFilledAutofillFields =
                new DatasetWithFilledAutofillFields();
        datasetWithFilledAutofillFields.autofillDataset = mDataset;
        datasetWithFilledAutofillFields.filledAutofillFields =
                Arrays.asList(mUsernameField, mPasswordField);
        datasetWithFilledAutofillFields.filledAutofillFields
                .sort(Comparator.comparing(FilledAutofillField::getFieldTypeName));

        // When inserting a page's autofill fields.
        mDatabase.autofillDao().insertAutofillDataset(mDataset);
        mDatabase.autofillDao().insertFilledAutofillFields(
                datasetWithFilledAutofillFields.filledAutofillFields);

        // Represents all hints of all fields on page.
        List<String> allHints = ImmutableList.of(View.AUTOFILL_HINT_USERNAME,
                View.AUTOFILL_HINT_PASSWORD);
        List<DatasetWithFilledAutofillFields> loadedDatasets = mDatabase.autofillDao()
                .getDatasets(allHints);
        loadedDatasets.get(0).filledAutofillFields.sort(
                Comparator.comparing(FilledAutofillField::getFieldTypeName));
        assertThat(loadedDatasets, contains(datasetWithFilledAutofillFields));
        assertThat(loadedDatasets, hasSize(1));
    }
}
