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
package com.example.android.autofill.app.edgecases;

import android.view.View;

import com.example.android.autofill.app.R;
import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultipleStepsCreditCardActivity extends AbstractMultipleStepsActivity {

    @Override
    protected Map<Integer, String> getStepsMap() {
        LinkedHashMap<Integer, String> steps = new LinkedHashMap<>(4);
        steps.put(R.string.credit_card_number_label,
                View.AUTOFILL_HINT_CREDIT_CARD_NUMBER);
        steps.put(R.string.credit_card_expiration_month_label,
                View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH);
        steps.put(R.string.credit_card_expiration_year_label,
                View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR);
        steps.put(R.string.credit_card_security_code_abbrev_label,
                View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE);
        return ImmutableMap.copyOf(steps);
    }
}
