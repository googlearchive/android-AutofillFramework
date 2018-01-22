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

import android.support.annotation.DrawableRes;
import android.widget.RemoteViews;

/**
 * This is a class containing helper methods for building Autofill Datasets and Responses.
 */
public final class RemoteViewsHelper {
    private RemoteViewsHelper() {
    }

    public static RemoteViews viewsWithAuth(String packageName, String text) {
        return simpleRemoteViews(packageName, text, R.drawable.ic_lock_black_24dp);
    }

    public static RemoteViews viewsWithNoAuth(String packageName, String text) {
        return simpleRemoteViews(packageName, text, R.drawable.ic_person_black_24dp);
    }

    private static RemoteViews simpleRemoteViews(String packageName, String remoteViewsText,
            @DrawableRes int drawableId) {
        RemoteViews presentation = new RemoteViews(packageName,
                R.layout.multidataset_service_list_item);
        presentation.setTextViewText(R.id.text, remoteViewsText);
        presentation.setImageViewResource(R.id.icon, drawableId);
        return presentation;
    }
}
