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
package com.example.android.autofillframework;

import android.os.Bundle;

import java.util.Arrays;
import java.util.Set;

public final class CommonUtil {

    public static final String TAG = "AutofillSample";
    public static final boolean DEBUG = true;
    public static final String EXTRA_DATASET_NAME = "dataset_name";
    public static final String EXTRA_FOR_RESPONSE = "for_response";

    private static void bundleToString(StringBuilder builder, Bundle data) {
        final Set<String> keySet = data.keySet();
        builder.append("[Bundle with ").append(keySet.size()).append(" keys:");
        for (String key : keySet) {
            builder.append(' ').append(key).append('=');
            Object value = data.get(key);
            if ((value instanceof Bundle)) {
                bundleToString(builder, (Bundle) value);
            } else {
                builder.append((value instanceof Object[])
                        ? Arrays.toString((Object[]) value) : value);
            }
        }
        builder.append(']');
    }

    public static String bundleToString(Bundle data) {
        if (data == null) {
            return "N/A";
        }
        final StringBuilder builder = new StringBuilder();
        bundleToString(builder, data);
        return builder.toString();
    }
}