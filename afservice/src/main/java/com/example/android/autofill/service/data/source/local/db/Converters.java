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

package com.example.android.autofill.service.data.source.local.db;

import android.arch.persistence.room.TypeConverter;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Type converter for Room database.
 */
public class Converters {

    /**
     * If database returns a {@link String} containing a comma delimited list of ints, this converts
     * the {@link String} to an {@link IntList}.
     */
    @TypeConverter
    public static IntList storedStringToIntList(String value) {
        List<String> strings = Arrays.asList(value.split("\\s*,\\s*"));
        List<Integer> ints = strings.stream().map(Integer::parseInt).collect(toList());
        return new IntList(ints);
    }

    /**
     * Converts the {@link IntList} back into a String containing a comma delimited list of
     * ints.
     */
    @TypeConverter
    public static String intListToStoredString(IntList list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer integer : list.ints) {
            stringBuilder.append(integer).append(",");
        }
        return stringBuilder.toString();
    }

    /**
     * If database returns a {@link String} containing a comma delimited list of Strings, this
     * converts the {@link String} to a {@link StringList}.
     */
    @TypeConverter
    public static StringList storedStringToStringList(String value) {
        List<String> strings = Arrays.asList(value.split("\\s*,\\s*"));
        return new StringList(strings);
    }


    /**
     * Converts the {@link StringList} back into a {@link String} containing a comma delimited
     * list of {@link String}s.
     */
    @TypeConverter
    public static String stringListToStoredString(StringList list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : list.strings) {
            stringBuilder.append(string).append(",");
        }
        return stringBuilder.toString();
    }

    /**
     * Wrapper class for {@code List<Integer>} so it can work with Room type converters.
     */
    public static class IntList {
        public final List<Integer> ints;

        public IntList(List<Integer> ints) {
            this.ints = ints;
        }
    }

    /**
     * Wrapper class for {@code List<String>} so it can work with Room type converters.
     */
    public static class StringList {
        public final List<String> strings;

        public StringList(List<String> ints) {
            this.strings = ints;
        }
    }
}
