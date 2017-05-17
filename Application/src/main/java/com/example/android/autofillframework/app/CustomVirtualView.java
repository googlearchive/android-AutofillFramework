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
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.autofillframework.R;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.android.autofillframework.CommonUtil.bundleToString;


/**
 * Custom View with virtual child views for Username/Password text fields.
 */
public class CustomVirtualView extends View {

    private static final String TAG = "CustomView";

    private static int nextId;

    private final ArrayList<Line> mLines = new ArrayList<>();
    private final SparseArray<Item> mItems = new SparseArray<>();
    private final AutofillManager mAfm;

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

        mAfm = context.getSystemService(AutofillManager.class);

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
        mUsernameLine = addLine("usernameField", context.getString(R.string.username_label),
                new String[] {View.AUTOFILL_HINT_USERNAME}, "         ", true);
        mPasswordLine = addLine("passwordField", context.getString(R.string.password_label),
                new String[] {View.AUTOFILL_HINT_PASSWORD}, "         ", false);

        Log.d(TAG, "Text height: " + mTextHeight);
    }

    @Override
    public void autofill(SparseArray<AutofillValue> values) {
        // User has just selected a Dataset from the list of Autofill suggestions and the Dataset's
        // AutofillValue gets passed into this method.
        Log.d(TAG, "autoFill(): " + values);
        for (int i = 0; i < values.size(); i++) {
            final int id = values.keyAt(i);
            final AutofillValue value = values.valueAt(i);
            final Item item = mItems.get(id);
            if (item == null) {
                Log.w(TAG, "No item for id " + id);
                return;
            }
            if (!item.editable) {
                Log.w(TAG, "Item for id " + id + " is not editable: " + item);
                return;
            }
            // Set the item's text to the text wrapped in the AutofillValue.
            item.text = value.getTextValue();
        }
        postInvalidate();
    }

    @Override
    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
        // Build a ViewStructure to pack in AutoFillService requests.
        structure.setClassName(getClass().getName());
        int childrenSize = mItems.size();
        Log.d(TAG, "onProvideAutofillVirtualStructure(): flags = " + flags + ", items = "
                + childrenSize + ", extras: " + bundleToString(structure.getExtras()));
        int index = structure.addChildCount(childrenSize);
        for (int i = 0; i < childrenSize; i++) {
            Item item = mItems.valueAt(i);
            Log.d(TAG, "Adding new child at index " + index + ": " + item);
            ViewStructure child = structure.newChild(index);
            child.setAutofillId(structure, item.id);
            child.setAutofillHints(item.hints);
            child.setAutofillType(item.type);
            child.setDataIsSensitive(!item.sanitized);
            child.setText(item.text);
            child.setAutofillValue(AutofillValue.forText(item.text));
            child.setFocused(item.focused);
            child.setId(item.id, getContext().getPackageName(), null, item.line.idEntry);
            child.setClassName(item.getClassName());
            index++;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw: " + mLines.size() + " lines; canvas:" + canvas);
        float x;
        float y = mTopMargin + mLineLength;
        for (int i = 0; i < mLines.size(); i++) {
            x = mLeftMargin;
            Line line = mLines.get(i);
            Log.v(TAG, "Drawing '" + line + "' at " + x + "x" + y);
            mTextPaint.setColor(line.fieldTextItem.focused ? mFocusedColor : mUnfocusedColor);
            String readOnlyText = line.labelItem.text + ":  [";
            String writeText = line.fieldTextItem.text + "]";
            // Paints the label first...
            canvas.drawText(readOnlyText, x, y, mTextPaint);
            // ...then paints the edit text and sets the proper boundary
            float deltaX = mTextPaint.measureText(readOnlyText);
            x += deltaX;
            line.bounds.set((int) x, (int) (y - mLineLength),
                    (int) (x + mTextPaint.measureText(writeText)), (int) y);
            Log.d(TAG, "setBounds(" + x + ", " + y + "): " + line.bounds);
            canvas.drawText(writeText, x, y, mTextPaint);
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

    private Line addLine(String idEntry, String label, String[] hints, String text, boolean sanitized) {
        Line line = new Line(idEntry, label, hints, text, sanitized);
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
        private final String[] hints;
        private final int type;
        private CharSequence text;
        private boolean focused = false;

        Item(Line line, int id, String[] hints, int type, CharSequence text, boolean editable, boolean sanitized) {
            this.line = line;
            this.id = id;
            this.text = text;
            this.editable = editable;
            this.sanitized = sanitized;
            this.hints = hints;
            this.type = type;
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

        // Boundaries of the text field, relative to the CustomView
        final Rect bounds = new Rect();
        private Item labelItem;
        private Item fieldTextItem;
        private String idEntry;

        private Line(String idEntry, String label, String[] hints, String text, boolean sanitized) {
            this.idEntry = idEntry;
            this.labelItem = new Item(this, ++nextId, null, AUTOFILL_TYPE_NONE, label, false, true);
            this.fieldTextItem = new Item(this, ++nextId, hints, AUTOFILL_TYPE_TEXT, text, true, sanitized);
        }

        void changeFocus(boolean focused) {
            fieldTextItem.focused = focused;
            if (focused) {
                final Rect absBounds = getAbsCoordinates();
                Log.d(TAG, "focus gained on " + fieldTextItem.id + "; absBounds=" + absBounds);
                mAfm.notifyViewEntered(CustomVirtualView.this, fieldTextItem.id, absBounds);
            } else {
                Log.d(TAG, "focus lost on " + fieldTextItem.id);
                mAfm.notifyViewExited(CustomVirtualView.this, fieldTextItem.id);
            }
        }

        private Rect getAbsCoordinates() {
            // Must offset the boundaries so they're relative to the CustomView.
            final int offset[] = new int[2];
            getLocationOnScreen(offset);
            final Rect absBounds = new Rect(bounds.left + offset[0],
                    bounds.top + offset[1],
                    bounds.right + offset[0], bounds.bottom + offset[1]);
            Log.v(TAG, "getAbsCoordinates() for " + fieldTextItem.id + ": bounds=" + bounds
                    + " offset: " + Arrays.toString(offset) + " absBounds: " + absBounds);
            return absBounds;
        }

        public void reset() {
            fieldTextItem.text = "        ";
        }

        @Override
        public String toString() {
            return "Label: " + labelItem + " Text: " + fieldTextItem + " Focused: " +
                    fieldTextItem.focused;
        }
    }
}