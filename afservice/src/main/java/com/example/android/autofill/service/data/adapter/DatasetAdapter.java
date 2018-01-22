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

package com.example.android.autofill.service.data.adapter;

import android.app.assist.AssistStructure;
import android.content.IntentSender;
import android.service.autofill.Dataset;
import android.util.MutableBoolean;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import com.example.android.autofill.service.AutofillHints;
import com.example.android.autofill.service.ClientParser;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FieldType;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.model.FilledAutofillField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.example.android.autofill.service.util.Util.indexOf;
import static com.example.android.autofill.service.util.Util.logv;
import static com.example.android.autofill.service.util.Util.logw;
import static java.util.stream.Collectors.toMap;

public class DatasetAdapter {
    private final ClientParser mClientParser;

    public DatasetAdapter(ClientParser clientParser) {
        mClientParser = clientParser;
    }

    /**
     * Wraps autofill data in a {@link Dataset} object which can then be sent back to the client.
     */
    public Dataset buildDataset(HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint,
            DatasetWithFilledAutofillFields datasetWithFilledAutofillFields,
            RemoteViews remoteViews) {
        return buildDataset(fieldTypesByAutofillHint, datasetWithFilledAutofillFields, remoteViews,
                null);
    }

    public Dataset buildDatasetForFocusedNode(FilledAutofillField filledAutofillField,
            FieldType fieldType, RemoteViews remoteViews) {
        Dataset.Builder datasetBuilder = new Dataset.Builder(remoteViews);
        boolean setAtLeastOneValue = bindDatasetToFocusedNode(filledAutofillField,
                fieldType, datasetBuilder);
        if (!setAtLeastOneValue) {
            return null;
        }
        return datasetBuilder.build();
    }

    /**
     * Wraps autofill data in a {@link Dataset} object with an IntentSender, which can then be
     * sent back to the client.
     */
    public Dataset buildDataset(HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint,
            DatasetWithFilledAutofillFields datasetWithFilledAutofillFields,
            RemoteViews remoteViews, IntentSender intentSender) {
        Dataset.Builder datasetBuilder = new Dataset.Builder(remoteViews);
        if (intentSender != null) {
            datasetBuilder.setAuthentication(intentSender);
        }
        boolean setAtLeastOneValue = bindDataset(fieldTypesByAutofillHint,
                datasetWithFilledAutofillFields, datasetBuilder);
        if (!setAtLeastOneValue) {
            return null;
        }
        return datasetBuilder.build();
    }

    /**
     * Build an autofill {@link Dataset} using saved data and the client's AssistStructure.
     */
    private boolean bindDataset(HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint,
            DatasetWithFilledAutofillFields datasetWithFilledAutofillFields,
            Dataset.Builder datasetBuilder) {
        MutableBoolean setValueAtLeastOnce = new MutableBoolean(false);
        Map<String, FilledAutofillField> filledAutofillFieldsByTypeName =
                datasetWithFilledAutofillFields.filledAutofillFields.stream()
                        .collect(toMap(FilledAutofillField::getFieldTypeName, Function.identity()));
        mClientParser.parse((node) ->
                parseAutofillFields(node, fieldTypesByAutofillHint, filledAutofillFieldsByTypeName,
                        datasetBuilder, setValueAtLeastOnce)
        );
        return setValueAtLeastOnce.value;
    }

    private boolean bindDatasetToFocusedNode(FilledAutofillField field,
            FieldType fieldType, Dataset.Builder builder) {
        MutableBoolean setValueAtLeastOnce = new MutableBoolean(false);
        mClientParser.parse((node) -> {
            if (node.isFocused() && node.getAutofillId() != null) {
                bindValueToNode(node, field, builder, setValueAtLeastOnce);
            }
        });
        return setValueAtLeastOnce.value;
    }

    private void parseAutofillFields(AssistStructure.ViewNode viewNode,
            HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint,
            Map<String, FilledAutofillField> filledAutofillFieldsByTypeName,
            Dataset.Builder builder, MutableBoolean setValueAtLeastOnce) {
        String[] rawHints = viewNode.getAutofillHints();
        if (rawHints == null || rawHints.length == 0) {
            logv("No af hints at ViewNode - %s", viewNode.getIdEntry());
            return;
        }
        String fieldTypeName = AutofillHints.getFieldTypeNameFromAutofillHints(
                fieldTypesByAutofillHint, Arrays.asList(rawHints));
        if (fieldTypeName == null) {
            return;
        }
        FilledAutofillField field = filledAutofillFieldsByTypeName.get(fieldTypeName);
        if (field == null) {
            return;
        }
        bindValueToNode(viewNode, field, builder, setValueAtLeastOnce);
    }

    void bindValueToNode(AssistStructure.ViewNode viewNode,
            FilledAutofillField field, Dataset.Builder builder,
            MutableBoolean setValueAtLeastOnce) {
        AutofillId autofillId = viewNode.getAutofillId();
        if (autofillId == null) {
            logw("Autofill ID null for %s", viewNode.toString());
            return;
        }
        int autofillType = viewNode.getAutofillType();
        switch (autofillType) {
            case View.AUTOFILL_TYPE_LIST:
                CharSequence[] options = viewNode.getAutofillOptions();
                int listValue = -1;
                if (options != null) {
                    listValue = indexOf(viewNode.getAutofillOptions(), field.getTextValue());
                }
                if (listValue != -1) {
                    builder.setValue(autofillId, AutofillValue.forList(listValue));
                    setValueAtLeastOnce.value = true;
                }
                break;
            case View.AUTOFILL_TYPE_DATE:
                Long dateValue = field.getDateValue();
                if (dateValue != null) {
                    builder.setValue(autofillId, AutofillValue.forDate(dateValue));
                    setValueAtLeastOnce.value = true;
                }
                break;
            case View.AUTOFILL_TYPE_TEXT:
                String textValue = field.getTextValue();
                if (textValue != null) {
                    builder.setValue(autofillId, AutofillValue.forText(textValue));
                    setValueAtLeastOnce.value = true;
                }
                break;
            case View.AUTOFILL_TYPE_TOGGLE:
                Boolean toggleValue = field.getToggleValue();
                if (toggleValue != null) {
                    builder.setValue(autofillId, AutofillValue.forToggle(toggleValue));
                    setValueAtLeastOnce.value = true;
                }
                break;
            case View.AUTOFILL_TYPE_NONE:
            default:
                logw("Invalid autofill type - %d", autofillType);
                break;
        }
    }
}
