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
import android.app.assist.AssistStructure.ViewNode;
import android.app.assist.AssistStructure.WindowNode;
import android.util.Log;

import static com.example.android.autofillframework.CommonUtil.TAG;

/**
 * Parser for an AssistStructure object. This is invoked when the Autofill Service receives an
 * AssistStructure from the client Activity, representing its View hierarchy. In this basic
 * sample, it only parses the hierarchy looking for Username and Password fields based on their
 * resource IDs.
 */
final class StructureParser {

    // This simple AutoFill service is capable of parsing these fields:
    private final AutoFillField usernameField = new AutoFillField("usernameField");
    private final AutoFillField passwordField = new AutoFillField("passwordField");
    private final AssistStructure structure;


    StructureParser(AssistStructure structure) {
        this.structure = structure;
    }

    /**
     * Depth first search of AssistStructure in search of Views whose resource ID entry is
     * "usernameField" or "passwordField"
     */
    void parse() {
        Log.d(TAG, "Parsing structure for " + structure.getActivityComponent());
        final int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            WindowNode node = structure.getWindowNodeAt(i);
            ViewNode view = node.getRootViewNode();
            parseLocked(view);
        }
    }

    private void parseLocked(ViewNode view) {
        final String resourceId = view.getIdEntry();
        Log.d(TAG, "resourceId == " + resourceId);
        if (resourceId != null && resourceId.equals(usernameField.getDescription())) {
            usernameField.setFrom(view);
        } else if (resourceId != null && resourceId.equals(passwordField.getDescription())) {
            passwordField.setFrom(view);
        }
        final int childrenSize = view.getChildCount();
        if (childrenSize > 0) {
            for (int i = 0; i < childrenSize; i++) {
                parseLocked(view.getChildAt(i));
            }
        }
    }

    public AutoFillField getUsernameField() {
        return usernameField;
    }

    public AutoFillField getPasswordField() {
        return passwordField;
    }
}
