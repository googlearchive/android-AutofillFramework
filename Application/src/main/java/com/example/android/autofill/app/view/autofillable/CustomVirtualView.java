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

package com.example.android.autofill.app.view.autofillable;

import static com.example.android.autofill.app.Util.bundleToString;

import android.content.Context;
import android.graphics.Rect;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewStructure;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.Util;

import java.text.DateFormat;
import java.util.Date;

/**
 * A custom View with a virtual structure that implements the Autofill APIs.
 */
public class CustomVirtualView extends AbstractCustomVirtualView {

    private static final String TAG = "CustomView";

    protected final AutofillManager mAutofillManager;
    private final SparseArray<Partition> mPartitionsByAutofillId = new SparseArray<>();

    public CustomVirtualView(Context context) {
        this(context, null);
    }

    public CustomVirtualView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVirtualView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomVirtualView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mAutofillManager = context.getSystemService(AutofillManager.class);
    }

    @Override
    public void autofill(SparseArray<AutofillValue> values) {
        Context context = getContext();

        // User has just selected a Dataset from the list of autofill suggestions.
        // The Dataset is comprised of a list of AutofillValues, with each AutofillValue meant
        // to fill a specific autofillable view. Now we have to update the UI based on the
        // AutofillValues in the list, but first we make sure all autofilled values belong to the
        // same partition
        if (DEBUG) {
            Log.d(TAG, "autofill(): " + values);
        }

        // First get the name of all partitions in the values
        ArraySet<String> partitions = new ArraySet<>();
        for (int i = 0; i < values.size(); i++) {
            int id = values.keyAt(i);
            Partition partition = mPartitionsByAutofillId.get(id);
            if (partition == null) {
                showError(context.getString(R.string.message_autofill_no_partitions, id,
                        mPartitionsByAutofillId));
                return;
            }
            partitions.add(partition.mName);
        }

        // Then make sure they follow the Highlander rule (There can be only one)
        if (partitions.size() != 1) {
            showError(context.getString(R.string.message_autofill_blocked, partitions));
            return;
        }

        // Finally, autofill it.
        DateFormat df = android.text.format.DateFormat.getDateFormat(context);
        for (int i = 0; i < values.size(); i++) {
            int id = values.keyAt(i);
            AutofillValue value = values.valueAt(i);
            Item item = mVirtualViews.get(id);

            if (item == null) {
                Log.w(TAG, "No item for id " + id);
                continue;
            }

            if (!item.editable) {
                showError(context.getString(R.string.message_autofill_readonly, item.text));
                continue;
            }

            // Check if the type was properly set by the autofill service
            if (DEBUG) {
                Log.d(TAG, "Validating " + i
                        + ": expectedType=" + Util.getAutofillTypeAsString(item.type)
                        + "(" + item.type + "), value=" + value);
            }
            boolean valid = false;
            if (value.isText() && item.type == AUTOFILL_TYPE_TEXT) {
                item.text = value.getTextValue();
                valid = true;
            } else if (value.isDate() && item.type == AUTOFILL_TYPE_DATE) {
                item.text = df.format(new Date(value.getDateValue()));
                valid = true;
            } else {
                Log.w(TAG, "Unsupported type: " + value);
            }
            if (!valid) {
                item.text = context.getString(R.string.message_autofill_invalid);
            }
        }
        postInvalidate();
        showMessage(context.getString(R.string.message_autofill_ok, partitions.valueAt(0)));
    }

    @Override
    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
        // Build a ViewStructure that will get passed to the AutofillService by the framework
        // when it is time to find autofill suggestions.
        structure.setClassName(getClass().getName());
        int childrenSize = mVirtualViews.size();
        if (DEBUG) {
            Log.d(TAG, "onProvideAutofillVirtualStructure(): flags = " + flags + ", items = "
                    + childrenSize + ", extras: " + bundleToString(structure.getExtras()));
        }
        int index = structure.addChildCount(childrenSize);
        // Traverse through the view hierarchy, including virtual child views. For each view, we
        // need to set the relevant autofill metadata and add it to the ViewStructure.
        for (int i = 0; i < childrenSize; i++) {
            Item item = mVirtualViews.valueAt(i);
            if (DEBUG) {
                Log.d(TAG, "Adding new child at index " + index + ": " + item);
            }
            ViewStructure child = structure.newChild(index);
            child.setAutofillId(structure.getAutofillId(), item.id);
            child.setAutofillHints(item.hints);
            child.setAutofillType(item.type);
            child.setAutofillValue(item.getAutofillValue());
            child.setDataIsSensitive(!item.sanitized);
            child.setFocused(item.focused);
            child.setVisibility(View.VISIBLE);
            child.setDimens(item.line.mBounds.left, item.line.mBounds.top, 0, 0,
                    item.line.mBounds.width(), item.line.mBounds.height());
            child.setId(item.id, getContext().getPackageName(), null, item.idEntry);
            child.setClassName(item.getClassName());
            child.setDimens(item.line.mBounds.left, item.line.mBounds.top, 0, 0,
                    item.line.mBounds.width(), item.line.mBounds.height());
            index++;
        }
    }

    @Override
    protected void notifyFocusGained(int virtualId, Rect bounds) {
        mAutofillManager.notifyViewEntered(this, virtualId, bounds);
    }

    @Override
    protected void notifyFocusLost(int virtualId) {
        mAutofillManager.notifyViewExited(this, virtualId);
    }

    @Override
    protected void onLineAdded(int id, Partition partition) {
        mPartitionsByAutofillId.put(id, partition);
    }
}
