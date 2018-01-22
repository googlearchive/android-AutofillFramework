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

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.autofill.service.data.source.local.dao.AutofillDao;
import com.example.android.autofill.service.data.source.local.db.AutofillDatabase;
import com.example.android.autofill.service.util.SingleExecutors;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalDataSourceTest {

    private LocalAutofillDataSource mLocalDataSource;
    private AutofillDatabase mDatabase;

    @Before
    public void setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                AutofillDatabase.class)
                .build();
        AutofillDao tasksDao = mDatabase.autofillDao();
        SharedPreferences sharedPreferences = InstrumentationRegistry.getContext()
                .getSharedPreferences(LocalAutofillDataSource.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        // Make sure that we're not keeping a reference to the wrong instance.
        LocalAutofillDataSource.clearInstance();
        mLocalDataSource = LocalAutofillDataSource.getInstance(sharedPreferences,
                tasksDao, new SingleExecutors());
    }

    @After
    public void cleanUp() {
        try {
            mDatabase.close();
        } finally {
            LocalAutofillDataSource.clearInstance();
        }
    }
}
