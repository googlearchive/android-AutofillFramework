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
package com.example.android.autofillframework.service;

import android.app.assist.AssistStructure;
import android.view.autofill.AutoFillId;

/**
 * Class that represents a field that can be autofilled. It will contain a description
 * (what type data the field holds), an AutoFillId (an ID unique to the rest of the ViewStructure),
 * and a value (what data is currently in the field).
 */
public class AutoFillField {
    private final String description;
    private AutoFillId id;

    // For simplicity, we will only support text values.
    private String value;

    public AutoFillField(String description) {
        this.description = description;
    }

    void setFrom(AssistStructure.ViewNode view) {
        id = view.getAutoFillId();
        CharSequence text = view.getText();
        value = text == null ? null : text.toString();
    }

    public String getDescription() {
        return description;
    }

    public AutoFillId getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "AutoFillField: [id=" + id + ", value=" + value + "]";
    }

}
