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
package com.example.android.autofillframework.service.datasource;

import com.example.android.autofillframework.service.model.ClientFormData;

import java.util.HashMap;
import java.util.List;

public interface AutofillRepository {

    /**
     * Gets saved ClientFormData that contains some objects that can autofill fields with these
     * {@code autofillHints}.
     */
    HashMap<String, ClientFormData> getClientFormData(List<String> focusedAutofillHints,
            List<String> allAutofillHints);

    /**
     * Saves LoginCredential under this datasetName.
     */
    void saveClientFormData(ClientFormData clientFormData);

    /**
     * Clears all data.
     */
    void clear();
}
