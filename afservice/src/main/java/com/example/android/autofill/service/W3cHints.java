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

public final class W3cHints {
    // Optional W3C prefixes
    public static final String PREFIX_SECTION = "section-";
    public static final String SHIPPING = "shipping";
    public static final String BILLING = "billing";
    // W3C prefixes below...
    public static final String PREFIX_HOME = "home";
    public static final String PREFIX_WORK = "work";
    public static final String PREFIX_FAX = "fax";
    public static final String PREFIX_PAGER = "pager";
    // ... require those suffix
    public static final String TEL = "tel";
    public static final String TEL_COUNTRY_CODE = "tel-country-code";
    public static final String TEL_NATIONAL = "tel-national";
    public static final String TEL_AREA_CODE = "tel-area-code";
    public static final String TEL_LOCAL = "tel-local";
    public static final String TEL_LOCAL_PREFIX = "tel-local-prefix";
    public static final String TEL_LOCAL_SUFFIX = "tel-local-suffix";
    public static final String TEL_EXTENSION = "tel_extension";
    public static final String EMAIL = "email";
    public static final String IMPP = "impp";

    private W3cHints() {
    }
}