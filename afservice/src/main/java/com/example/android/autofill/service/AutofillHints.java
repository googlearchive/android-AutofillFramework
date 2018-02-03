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
package com.example.android.autofill.service;

import android.support.annotation.NonNull;

import com.example.android.autofill.service.model.FakeData;
import com.example.android.autofill.service.model.FieldType;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.model.FilledAutofillField;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.example.android.autofill.service.util.Util.logd;
import static com.example.android.autofill.service.util.Util.logw;
import static java.util.stream.Collectors.toList;

public final class AutofillHints {
    public static final int PARTITION_ALL = -1;
    public static final int PARTITION_OTHER = 0;
    public static final int PARTITION_ADDRESS = 1;
    public static final int PARTITION_EMAIL = 2;
    public static final int PARTITION_CREDIT_CARD = 3;
    public static final int[] PARTITIONS = {
            PARTITION_OTHER, PARTITION_ADDRESS, PARTITION_EMAIL, PARTITION_CREDIT_CARD
    };

    private AutofillHints() {
    }

    public static FilledAutofillField generateFakeField(
            FieldTypeWithHeuristics fieldTypeWithHeuristics, String packageName, int seed,
            String datasetId) {
        FakeData fakeData = fieldTypeWithHeuristics.fieldType.getFakeData();
        String fieldTypeName = fieldTypeWithHeuristics.fieldType.getTypeName();
        String text = null;
        Long date = null;
        Boolean toggle = null;
        if (fakeData.strictExampleSet != null && fakeData.strictExampleSet.strings != null &&
                fakeData.strictExampleSet.strings.size() > 0 &&
                !fakeData.strictExampleSet.strings.get(0).isEmpty()) {
            List<String> choices = fakeData.strictExampleSet.strings;
            text = choices.get(seed % choices.size());
        } else if (fakeData.textTemplate != null) {
            text = fakeData.textTemplate.replace("seed", "" + seed)
                    .replace("curr_time", "" + Calendar.getInstance().getTimeInMillis());
        } else if (fakeData.dateTemplate != null) {
            if (fakeData.dateTemplate.contains("curr_time")) {
                date = Calendar.getInstance().getTimeInMillis();
            }
        }
        return new FilledAutofillField(datasetId, fieldTypeName, text, date, toggle);
    }

    public static String getFieldTypeNameFromAutofillHints(
            HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint,
            @NonNull List<String> hints) {
        return getFieldTypeNameFromAutofillHints(fieldTypesByAutofillHint, hints, PARTITION_ALL);
    }

    public static String getFieldTypeNameFromAutofillHints(
            HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint,
            @NonNull List<String> hints, int partition) {
        List<String> fieldTypeNames = removePrefixes(hints)
                .stream()
                .filter(fieldTypesByAutofillHint::containsKey)
                .map(fieldTypesByAutofillHint::get)
                .filter(Objects::nonNull)
                .filter((fieldTypeWithHints) ->
                        matchesPartition(fieldTypeWithHints.fieldType.getPartition(), partition))
                .map(FieldTypeWithHeuristics::getFieldType).map(FieldType::getTypeName)
                .collect(toList());
        if (fieldTypeNames != null && fieldTypeNames.size() > 0) {
            return fieldTypeNames.get(0);
        } else {
            return null;
        }
    }

    public static boolean matchesPartition(int partition, int otherPartition) {
        return partition == PARTITION_ALL || otherPartition == PARTITION_ALL ||
                partition == otherPartition;
    }

    private static List<String> removePrefixes(@NonNull List<String> hints) {
        List<String> hintsWithoutPrefixes = new ArrayList<>();
        String nextHint = null;
        for (int i = 0; i < hints.size(); i++) {
            String hint = hints.get(i);
            if (i < hints.size() - 1) {
                nextHint = hints.get(i + 1);
            }
            // First convert the compound W3C autofill hints
            if (isW3cSectionPrefix(hint) && i < hints.size() - 1) {
                i++;
                hint = hints.get(i);
                logd("Hint is a W3C section prefix; using %s instead", hint);
                if (i < hints.size() - 1) {
                    nextHint = hints.get(i + 1);
                }
            }
            if (isW3cTypePrefix(hint) && nextHint != null && isW3cTypeHint(nextHint)) {
                hint = nextHint;
                i++;
                logd("Hint is a W3C type prefix; using %s instead", hint);
            }
            if (isW3cAddressType(hint) && nextHint != null) {
                hint = nextHint;
                i++;
                logd("Hint is a W3C address prefix; using %s instead", hint);
            }
            hintsWithoutPrefixes.add(hint);
        }
        return hintsWithoutPrefixes;
    }

    private static boolean isW3cSectionPrefix(@NonNull String hint) {
        return hint.startsWith(W3cHints.PREFIX_SECTION);
    }

    private static boolean isW3cAddressType(@NonNull String hint) {
        switch (hint) {
            case W3cHints.SHIPPING:
            case W3cHints.BILLING:
                return true;
        }
        return false;
    }

    private static boolean isW3cTypePrefix(@NonNull String hint) {
        switch (hint) {
            case W3cHints.PREFIX_WORK:
            case W3cHints.PREFIX_FAX:
            case W3cHints.PREFIX_HOME:
            case W3cHints.PREFIX_PAGER:
                return true;
        }
        return false;
    }

    private static boolean isW3cTypeHint(@NonNull String hint) {
        switch (hint) {
            case W3cHints.TEL:
            case W3cHints.TEL_COUNTRY_CODE:
            case W3cHints.TEL_NATIONAL:
            case W3cHints.TEL_AREA_CODE:
            case W3cHints.TEL_LOCAL:
            case W3cHints.TEL_LOCAL_PREFIX:
            case W3cHints.TEL_LOCAL_SUFFIX:
            case W3cHints.TEL_EXTENSION:
            case W3cHints.EMAIL:
            case W3cHints.IMPP:
                return true;
        }
        logw("Invalid W3C type hint: %s", hint);
        return false;
    }
}
