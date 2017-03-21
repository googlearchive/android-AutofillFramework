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
package com.example.android.autofillframework.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStructure;
import android.view.autofill.AutoFillManager;
import android.view.autofill.AutoFillValue;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.autofillframework.R;

import java.util.ArrayList;

import static com.example.android.autofillframework.CommonUtil.bundleToString;


/**
 * Custom View with virtual child views for Username/Password text fields.
 */

public class CustomVirtualView extends View {

    private static final String TAG = "CustomView";

    private static int nextId;

    private final ArrayList<Line> mLines = new ArrayList<>();
    private final SparseArray<Item> mItems = new SparseArray<>();
    private final AutoFillManager mAfm;

    private Line mFocusedLine;
    private Paint mTextPaint;
    private int mTextHeight;
    private int mTopMargin;
    private int mLeftMargin;
    private int mVerticalGap;
    private int mLineLength;
    private int mFocusedColor;
    private int mUnfocusedColor;

    private Line mUsernameLine;
    private Line mPasswordLine;

    public CustomVirtualView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAfm = context.getSystemService(AutoFillManager.class);

        mTextPaint = new Paint();

        mUnfocusedColor = Color.BLACK;
        mFocusedColor = Color.RED;
        mTextPaint.setStyle(Style.FILL);
        mTopMargin = 100;
        mLeftMargin = 100;
        mTextHeight = 90;
        mVerticalGap = 10;

        mLineLength = mTextHeight + mVerticalGap;
        mTextPaint.setTextSize(mTextHeight);
        mUsernameLine = addLine("usernameField", context.getString(R.string.username_label), "         ", true);
        mPasswordLine = addLine("passwordField", context.getString(R.string.password_label), "         ", false);

        Log.d(TAG, "Text height: " + mTextHeight);
    }

    @Override
    public void autoFillVirtual(int id, AutoFillValue value) {
        // User has just selected a Dataset from the list of Autofill suggestions and the Dataset's
        // AutoFillValue gets passed into this method.
        Log.d(TAG, "autoFillVirtual: id=" + id + ", value=" + value);
        Item item = mItems.get(id);
        if (item == null) {
            Log.w(TAG, "No item for id " + id);
            return;
        }
        if (!item.editable) {
            Log.w(TAG, "Item for id " + id + " is not editable: " + item);
            return;
        }
        // Set the item's text to the text wrapped in the AutoFillValue.
        item.text = value.getTextValue();
        postInvalidate();
    }

    @Override
    public void onProvideAutoFillVirtualStructure(ViewStructure structure, int flags) {
        // Build a ViewStructure to pack in AutoFillService requests.
        structure.setClassName(getClass().getName());
        int childrenSize = mItems.size();
        Log.d(TAG, "onProvideAutoFillVirtualStructure(): flags = " + flags + ", items = "
                + childrenSize + ", extras: " + bundleToString(structure.getExtras()));
        int index = structure.addChildCount(childrenSize);
        for (int i = 0; i < childrenSize; i++) {
            Item item = mItems.valueAt(i);
            Log.d(TAG, "Adding new child at index " + index + ": " + item);
            ViewStructure child = structure.newChild(index, item.id, flags);
            child.setSanitized(item.sanitized);
            child.setText(item.text);
            child.setAutoFillValue(AutoFillValue.forText(item.text));
            child.setFocused(item.line.focused);
            child.setId(item.id, getContext().getPackageName(), null, item.line.idEntry);
            child.setClassName(item.getClassName());
            index++;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw: " + mLines.size() + " lines; canvas:" + canvas);
        float x = mLeftMargin;
        float y = mTopMargin + mLineLength;
        for (int i = 0; i < mLines.size(); i++) {
            final Line line = mLines.get(i);
            Log.v(TAG, "Drawing '" + line + "' at " + x + "x" + y);
            mTextPaint.setColor(line.focused ? mFocusedColor : mUnfocusedColor);
            final String text = line.labelItem.text + ":  [" + line.fieldTextItem.text + "]";
            canvas.drawText(text, x, y, mTextPaint);
            line.setBounds(x, y);
            y += mLineLength;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        Log.d(TAG, "Touched: y=" + y + ", range=" + mLineLength + ", top=" + mTopMargin);
        int lowerY = mTopMargin;
        int upperY = -1;
        for (int i = 0; i < mLines.size(); i++) {
            upperY = lowerY + mLineLength;
            Line line = mLines.get(i);
            Log.d(TAG, "Line " + i + " ranges from " + lowerY + " to " + upperY);
            if (lowerY <= y && y <= upperY) {
                if (mFocusedLine != null) {
                    Log.d(TAG, "Removing focus from " + mFocusedLine);
                    mFocusedLine.changeFocus(false);
                }
                Log.d(TAG, "Changing focus to " + line);
                mFocusedLine = line;
                mFocusedLine.changeFocus(true);
                invalidate();
                break;
            }
            lowerY += mLineLength;
        }
        return super.onTouchEvent(event);
    }

    public CharSequence getUsernameText() {
        return mUsernameLine.fieldTextItem.text;
    }

    public CharSequence getPasswordText() {
        return mPasswordLine.fieldTextItem.text;
    }

    public void resetFields() {
        mUsernameLine.reset();
        mPasswordLine.reset();
        postInvalidate();
    }

    private Line addLine(String idEntry, String label, String text, boolean sanitized) {
        Line line = new Line(idEntry, label, text, sanitized);
        mLines.add(line);
        mItems.put(line.labelItem.id, line.labelItem);
        mItems.put(line.fieldTextItem.id, line.fieldTextItem);
        return line;
    }

    private static final class Item {
        private final Line line;
        private final int id;
        private final boolean editable;
        private final boolean sanitized;
        private CharSequence text;

        Item(Line line, int id, CharSequence text, boolean editable, boolean sanitized) {
            this.line = line;
            this.id = id;
            this.text = text;
            this.editable = editable;
            this.sanitized = sanitized;
        }

        @Override
        public String toString() {
            return id + ": " + text + (editable ? " (editable)" : " (read-only)"
                    + (sanitized ? " (sanitized)" : " (sensitive"));
        }

        public String getClassName() {
            return editable ? EditText.class.getName() : TextView.class.getName();
        }
    }

    private final class Line {

        private Item labelItem;
        private Item fieldTextItem;
        private String idEntry;

        private Rect bounds;

        private boolean focused;

        private Line(String idEntry, String label, String text, boolean sanitized) {
            this.idEntry = idEntry;
            this.labelItem = new Item(this, ++nextId, label, false, true);
            this.fieldTextItem = new Item(this, ++nextId, text, true, sanitized);
        }

        void setBounds(float x, float y) {
            // This determines the location / size of the autofill dropdown because we pass these
            // bounds in the AutoFillManager.virtualFocusChanged() call.
            int left = (int) (x + mTextPaint.measureText(labelItem.text.toString() + ": ["));
            int right = (int) (left + mTextPaint.measureText(fieldTextItem.text.toString()));
            int top = (int) (y + mTextHeight);
            int bottom = (int) (y + 3 * mTextHeight);
            if (bounds == null) {
                bounds = new Rect(left, top, right, bottom);
            } else {
                bounds.set(left, top, right, bottom);
            }
            Log.d(TAG, "setBounds(" + x + ", " + y + "): " + bounds);
        }

        void changeFocus(boolean focused) {
            Log.d(TAG, "onChangeFocus() on " + fieldTextItem.id + ": " + focused + " bounds: " + bounds);
            this.focused = focused;
            mAfm.virtualFocusChanged(CustomVirtualView.this, fieldTextItem.id, bounds, focused);
        }

        public void reset() {
            fieldTextItem.text = "        ";
        }

        @Override
        public String toString() {
            return "Label: " + labelItem + " Text: " + fieldTextItem + " Focused: " + focused;
        }
    }
}
