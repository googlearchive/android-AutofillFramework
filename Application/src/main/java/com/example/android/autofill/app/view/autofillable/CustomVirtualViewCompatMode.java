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

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

/**
 * A custom View with a virtual structure that implements the Accessibility APIs.
 *
 * <p><b>Note:</b> this class is useful to test an Autofill service that supports Compatibility
 * Mode; real applications with a virtual structure should explicitly support Autofill by
 * implementing its APIs as {@link CustomVirtualView} does.
 */
public class CustomVirtualViewCompatMode extends AbstractCustomVirtualView {

    private static final String TAG = "CustomVirtualViewCompatMode";

    private final AccessibilityDelegate mAccessibilityDelegate;
    private final AccessibilityNodeProvider mAccessibilityNodeProvider;

    public CustomVirtualViewCompatMode(Context context) {
        this(context, null);
    }

    public CustomVirtualViewCompatMode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVirtualViewCompatMode(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomVirtualViewCompatMode(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mAccessibilityNodeProvider = new AccessibilityNodeProvider() {
            @Override
            public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
                if (DEBUG) {
                    Log.d(TAG, "createAccessibilityNodeInfo(): id=" + virtualViewId);
                }
                switch (virtualViewId) {
                    case AccessibilityNodeProvider.HOST_VIEW_ID:
                        return onProvideAutofillCompatModeAccessibilityNodeInfo();
                    default:
                        final Item item = getItem(virtualViewId);
                        return item.provideAccessibilityNodeInfo(CustomVirtualViewCompatMode.this,
                                getContext());
                }
            }

            @Override
            public boolean performAction(int virtualViewId, int action, Bundle arguments) {
                if (action == AccessibilityNodeInfo.ACTION_SET_TEXT) {
                    final CharSequence text = arguments.getCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE);
                    final Item item = getItem(virtualViewId);
                    item.setText(text);
                    invalidate();
                    return true;
                }

                return false;
            }
        };
        mAccessibilityDelegate = new AccessibilityDelegate() {
            @Override
            public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
                return mAccessibilityNodeProvider;
            }
        };
        setAccessibilityDelegate(mAccessibilityDelegate);
    }

    @Override
    protected void notifyFocusGained(int virtualId, Rect bounds) {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED, virtualId);
    }

    @Override
    protected void notifyFocusLost(int virtualId) {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED, virtualId);
    }

    private void sendAccessibilityEvent(int eventType, int virtualId) {
        AccessibilityEvent event = AccessibilityEvent.obtain();
        event.setEventType(eventType);
        event.setSource(this, virtualId);
        event.setEnabled(true);
        event.setPackageName(getContext().getPackageName());
        if (VERBOSE) {
            Log.v(TAG, "sendAccessibilityEvent(" + eventType + ", " + virtualId + "): " + event);
        }
        getContext().getSystemService(AccessibilityManager.class).sendAccessibilityEvent(event);
    }

    private AccessibilityNodeInfo onProvideAutofillCompatModeAccessibilityNodeInfo() {
        final AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();

        final String packageName = getContext().getPackageName();
        node.setPackageName(packageName);
        node.setClassName(getClass().getName());

        final int childrenSize = mVirtualViews.size();
        for (int i = 0; i < childrenSize; i++) {
            final Item item = mVirtualViews.valueAt(i);
            if (DEBUG) {
                Log.d(TAG, "Adding new A11Y child with id " + item.id + ": " + item);
            }
            node.addChild(this, item.id);
        }
        return node;
    }
}
