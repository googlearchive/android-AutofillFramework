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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * A version of {@link CustomVirtualView} that uses gesture to provide scrolling.
 */
public class ScrollableCustomVirtualView extends CustomVirtualView
        implements GestureDetector.OnGestureListener {

    private static final String TAG = "ScrollableCustomView";

    private GestureDetector mGestureDetector;

    public ScrollableCustomVirtualView(Context context) {
        this(context, null);
    }

    public ScrollableCustomVirtualView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ScrollableCustomVirtualView(Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ScrollableCustomVirtualView(Context context, @Nullable AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mGestureDetector = new GestureDetector(context, this);
    }

    /**
     * Resets the UI to the intial state.
     */
    public void resetPositions() {
        super.resetCoordinates();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /*
     * Methods below implement GestureDetector.OnGestureListener
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (VERBOSE) Log.v(TAG, "onScroll(): " + distanceX + " - " + distanceY);
        if (mFocusedLine != null) {
            mAutofillManager.notifyViewExited(this, mFocusedLine.mFieldTextItem.id);
        }
        mTopMargin -= distanceY;
        mLeftMargin -= distanceX;
        invalidate();
        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        onMotion((int) event.getY());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }
}