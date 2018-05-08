/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.example.android.autofill.service.simple;

import android.app.assist.AssistStructure.ViewNode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.android.autofill.service.MyAutofillService;

/**
 * A basic service that uses some rudimentary heuristics to identify fields that are not explicitly
 * marked with autofill hints.
 *
 * <p>The goal of this class is to provide a simple autofill service implementation that is easy
 * to understand and extend, but it should <strong>not</strong> be used as-is on real apps because
 * it lacks fundamental security requirements such as data partitioning and package verification
 * &mdashthese requirements are fullfilled by {@link MyAutofillService}. *
 */
public class BasicHeuristicsService extends BasicService {

    private static final String TAG = "BasicHeuristicsService";

    @Override
    @Nullable
    protected String getHint(@NonNull ViewNode node) {

        // First try the explicit autofill hints...

        String hint = super.getHint(node);
        if (hint != null) return hint;

        // Then try some rudimentary heuristics based on other node properties

        String viewHint = node.getHint();
        hint = inferHint(viewHint);
        if (hint != null) {
            Log.d(TAG, "Found hint using view hint(" + viewHint + "): " + hint);
            return hint;
        } else if (!TextUtils.isEmpty(viewHint)) {
            Log.v(TAG, "No hint using view hint: " + viewHint);
        }

        String resourceId = node.getIdEntry();
        hint = inferHint(resourceId);
        if (hint != null) {
            Log.d(TAG, "Found hint using resourceId(" + resourceId + "): " + hint);
            return hint;
        } else if (!TextUtils.isEmpty(resourceId)) {
            Log.v(TAG, "No hint using resourceId: " + resourceId);
        }

        CharSequence text = node.getText();
        CharSequence className = node.getClassName();
        if (text != null && className != null && className.toString().contains("EditText")) {
            hint = inferHint(text.toString());
            if (hint != null) {
                // NODE: text should not be logged, as it could contain PII
                Log.d(TAG, "Found hint using text(" + text + "): " + hint);
                return hint;
            }
        } else if (!TextUtils.isEmpty(text)) {
            // NODE: text should not be logged, as it could contain PII
            Log.v(TAG, "No hint using text: " + text + " and class " + className);
        }
        return null;
    }

    /**
     * Uses heuristics to infer an autofill hint from a {@code string}.
     *
     * @return standard autofill hint, or {@code null} when it could not be inferred.
     */
    @Nullable
    protected String inferHint(@Nullable String string) {
        if (string == null) return null;

        string = string.toLowerCase();
        if (string.contains("password")) return View.AUTOFILL_HINT_PASSWORD;
        if (string.contains("username")
                || (string.contains("login") && string.contains("id")))
            return View.AUTOFILL_HINT_USERNAME;
        if (string.contains("email")) return View.AUTOFILL_HINT_EMAIL_ADDRESS;
        if (string.contains("name")) return View.AUTOFILL_HINT_NAME;
        if (string.contains("phone")) return View.AUTOFILL_HINT_PHONE;
        if (string.contains("address")) {
            if (string.contains("1")) return "address-line1"; // W3C spec
            if (string.contains("2")) return "address-line2"; // W3C spec
            if (string.contains("3")) return "address-line3"; // W3C spec
            return View.AUTOFILL_HINT_POSTAL_ADDRESS;
        }
        if (string.contains("zip")) return View.AUTOFILL_HINT_POSTAL_CODE;
        if (string.contains("city")) return "address-level-2"; // W3C spec
        if (string.contains("state") || string.contains("province") || string.contains("region"))
            return "address-level-3"; // W3C spec
        if (string.contains("country")) return "country"; // W3C spec

        return null;
    }
}
