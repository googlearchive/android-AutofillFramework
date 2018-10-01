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
package com.example.android.autofill.app.view.autofillable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.autofill.AutofillValue;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.Util;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Base class for a custom view that manages its own virtual structure, i.e., this is a leaf
 * {@link View} in the activity's structure, and it draws its own child UI elements.
 *
 * <p>This class only draws the views and provides hooks to integrate them with Android APIs such
 * as Autofill and Accessibility&mdash;its up to the subclass to implement these integration points.
 */
abstract class AbstractCustomVirtualView extends View {

    protected static final boolean DEBUG = true;
    protected static final boolean VERBOSE = false;

    /**
     * When set, it notifies AutofillManager of focus change as the view scrolls, so the
     * autofill UI is continually drawn.
     * <p>
     * <p>This is janky and incompatible with the way the autofill UI works on native views, but
     * it's a cool experiment!
     */
    private static final boolean DRAW_AUTOFILL_UI_AFTER_SCROLL = false;

    private static final String TAG = "AbstractCustomVirtualView";
    private static final int DEFAULT_TEXT_HEIGHT_DP = 34;
    private static final int VERTICAL_GAP = 10;
    private static final int UNFOCUSED_COLOR = Color.BLACK;
    private static final int FOCUSED_COLOR = Color.RED;
    private static int sNextId;
    protected final ArrayList<Line> mVirtualViewGroups = new ArrayList<>();
    protected final SparseArray<Item> mVirtualViews = new SparseArray<>();
    private final ArrayMap<String, Partition> mPartitionsByName = new ArrayMap<>();
    protected Line mFocusedLine;
    protected int mTopMargin;
    protected int mLeftMargin;
    private Paint mTextPaint;
    private int mTextHeight;
    private int mLineLength;

    protected AbstractCustomVirtualView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mTextPaint = new Paint();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomVirtualView,
                defStyleAttr, defStyleRes);
        int defaultHeight =
                (int) (DEFAULT_TEXT_HEIGHT_DP * getResources().getDisplayMetrics().density);
        mTextHeight = typedArray.getDimensionPixelSize(
                R.styleable.CustomVirtualView_internalTextSize, defaultHeight);
        typedArray.recycle();
        resetCoordinates();
    }

    protected Item getItem(int id) {
        final Item item = mVirtualViews.get(id);
        Preconditions.checkArgument(item != null, "No item for id %s: %s", id, mVirtualViews);
        return item;
    }

    protected void resetCoordinates() {
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setTextSize(mTextHeight);
        mTopMargin = getPaddingTop();
        mLeftMargin = getPaddingStart();
        mLineLength = mTextHeight + VERTICAL_GAP;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (VERBOSE) {
            Log.v(TAG, "onDraw(): " + mVirtualViewGroups.size() + " lines; canvas:" + canvas);
        }
        float x;
        float y = mTopMargin + mLineLength;
        for (int i = 0; i < mVirtualViewGroups.size(); i++) {
            Line line = mVirtualViewGroups.get(i);
            x = mLeftMargin;
            if (VERBOSE) Log.v(TAG, "Drawing '" + line + "' at " + x + "x" + y);
            mTextPaint.setColor(line.mFieldTextItem.focused ? FOCUSED_COLOR : UNFOCUSED_COLOR);
            String readOnlyText = line.mLabelItem.text + ":  [";
            String writeText = line.mFieldTextItem.text + "]";
            // Paints the label first...
            canvas.drawText(readOnlyText, x, y, mTextPaint);
            // ...then paints the edit text and sets the proper boundary
            float deltaX = mTextPaint.measureText(readOnlyText);
            x += deltaX;
            line.mBounds.set((int) x, (int) (y - mLineLength),
                    (int) (x + mTextPaint.measureText(writeText)), (int) y);
            if (VERBOSE) Log.v(TAG, "setBounds(" + x + ", " + y + "): " + line.mBounds);
            canvas.drawText(writeText, x, y, mTextPaint);
            y += mLineLength;

            if (DRAW_AUTOFILL_UI_AFTER_SCROLL) {
                line.notifyFocusChanged();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        onMotion(y);
        return super.onTouchEvent(event);
    }

    /**
     * Handles a motion event.
     *
     * @param y y coordinate.
     */
    protected void onMotion(int y) {
        if (DEBUG) {
            Log.d(TAG, "onMotion(): y=" + y + ", range=" + mLineLength + ", top=" + mTopMargin);
        }
        int lowerY = mTopMargin;
        int upperY = -1;
        for (int i = 0; i < mVirtualViewGroups.size(); i++) {
            Line line = mVirtualViewGroups.get(i);
            upperY = lowerY + mLineLength;
            if (DEBUG) Log.d(TAG, "Line " + i + " ranges from " + lowerY + " to " + upperY);
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
    }

    /**
     * Creates a new partition with the given name.
     *
     * @throws IllegalArgumentException if such partition already exists.
     */
    public Partition addPartition(String name) {
        Preconditions.checkNotNull(name, "Name cannot be null.");
        Preconditions.checkArgument(!mPartitionsByName.containsKey(name),
                "Partition with such name already exists.");
        Partition partition = new Partition(name);
        mPartitionsByName.put(name, partition);
        return partition;
    }


    protected abstract void notifyFocusGained(int virtualId, Rect bounds);

    protected abstract void notifyFocusLost(int virtualId);

    protected void onLineAdded(int id, Partition partition) {
        if (VERBOSE) Log.v(TAG, "onLineAdded: id=" + id + ", partition=" + partition);
    }

    protected void showError(String message) {
        showMessage(true, message);
    }

    protected void showMessage(String message) {
        showMessage(false, message);
    }

    private void showMessage(boolean warning, String message) {
        if (warning) {
            Log.w(TAG, message);
        } else {
            Log.i(TAG, message);
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    protected static final class Item {
        public final int id;
        public final String idEntry;
        public final Line line;
        public final boolean editable;
        public final boolean sanitized;
        public final String[] hints;
        public final int type;
        public CharSequence text;
        public boolean focused = false;
        public long date;
        private TextWatcher mListener;

        Item(Line line, int id, String idEntry, String[] hints, int type, CharSequence text,
                boolean editable, boolean sanitized) {
            this.line = line;
            this.id = id;
            this.idEntry = idEntry;
            this.text = text;
            this.editable = editable;
            this.sanitized = sanitized;
            this.hints = hints;
            this.type = type;
        }

        @Override
        public String toString() {
            return id + "/" + idEntry + ": "
                    + (type == AUTOFILL_TYPE_DATE ? date : text) // TODO: use DateFormat for date
                    + " (" + Util.getAutofillTypeAsString(type) + ")"
                    + (editable ? " (editable)" : " (read-only)"
                    + (sanitized ? " (sanitized)" : " (sensitive"))
                    + (hints == null ? " (no hints)" : " ( " + Arrays.toString(hints) + ")");
        }

        protected String getClassName() {
            return editable ? EditText.class.getName() : TextView.class.getName();
        }

        protected AutofillValue getAutofillValue() {
            switch (type) {
                case AUTOFILL_TYPE_TEXT:
                    return (TextUtils.getTrimmedLength(text) > 0)
                            ? AutofillValue.forText(text)
                            : null;
                case AUTOFILL_TYPE_DATE:
                    return AutofillValue.forDate(date);
                default:
                    return null;
            }
        }

        protected AccessibilityNodeInfo provideAccessibilityNodeInfo(View parent, Context context) {
            final AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
            node.setSource(parent, id);
            node.setPackageName(context.getPackageName());
            node.setClassName(getClassName());
            node.setEditable(editable);
            node.setViewIdResourceName(idEntry);
            node.setVisibleToUser(true);
            final Rect absBounds = line.getAbsCoordinates();
            if (absBounds != null) {
                node.setBoundsInScreen(absBounds);
            }
            if (TextUtils.getTrimmedLength(text) > 0) {
                // TODO: Must checked trimmed length because input fields use 8 empty spaces to
                // set width
                node.setText(text);
            }
            return node;
        }

        protected void setText(CharSequence value) {
            if (!editable) {
                Log.w(TAG, "Item for id " + id + " is not editable: " + this);
                return;
            }
            text = value;
            if (mListener != null) {
                Log.d(TAG, "Notify listener: " + text);
                mListener.onTextChanged(text, 0, 0, 0);
            }
        }

    }

    /**
     * A partition represents a logical group of items, such as credit card info.
     */
    public final class Partition {
        protected final String mName;
        protected final SparseArray<Line> mLines = new SparseArray<>();

        private Partition(String name) {
            mName = name;
        }

        /**
         * Adds a new line (containining a label and an input field) to the view.
         *
         * @param idEntryPrefix id prefix used to identify the line - label node will be suffixed
         *                      with {@code Label} and editable node with {@code Field}.
         * @param autofillType  {@link View#getAutofillType() autofill type} of the field.
         * @param label         text used in the label.
         * @param text          initial text used in the input field.
         * @param sensitive     whether the input is considered sensitive.
         * @param autofillHints list of autofill hints.
         * @return the new line.
         */
        public Line addLine(String idEntryPrefix, int autofillType, String label, String text,
                boolean sensitive, String... autofillHints) {
            Preconditions.checkArgument(autofillType == AUTOFILL_TYPE_TEXT
                    || autofillType == AUTOFILL_TYPE_DATE, "Unsupported type: " + autofillType);
            Line line = new Line(idEntryPrefix, autofillType, label, autofillHints, text,
                    !sensitive);
            mVirtualViewGroups.add(line);
            int id = line.mFieldTextItem.id;
            mLines.put(id, line);
            mVirtualViews.put(line.mLabelItem.id, line.mLabelItem);
            mVirtualViews.put(id, line.mFieldTextItem);
            onLineAdded(id, this);

            return line;
        }

        /**
         * Resets the value of all items in the partition.
         */
        public void reset() {
            for (int i = 0; i < mLines.size(); i++) {
                mLines.valueAt(i).reset();
            }
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    /**
     * A line in the virtual view contains a label and an input field.
     */
    public final class Line {

        protected final Item mFieldTextItem;
        // Boundaries of the text field, relative to the CustomView
        protected final Rect mBounds = new Rect();
        protected final Item mLabelItem;
        protected final int mAutofillType;

        private Line(String idEntryPrefix, int autofillType, String label, String[] hints,
                String text, boolean sanitized) {
            this.mAutofillType = autofillType;
            this.mLabelItem = new Item(this, ++sNextId, idEntryPrefix + "Label", null,
                    AUTOFILL_TYPE_NONE, label, false, true);
            this.mFieldTextItem = new Item(this, ++sNextId, idEntryPrefix + "Field", hints,
                    autofillType, text, true, sanitized);
        }

        private void changeFocus(boolean focused) {
            mFieldTextItem.focused = focused;
            notifyFocusChanged();
        }

        void notifyFocusChanged() {
            if (mFieldTextItem.focused) {
                Rect absBounds = getAbsCoordinates();
                if (DEBUG) {
                    Log.d(TAG, "focus gained on " + mFieldTextItem.id + "; absBounds=" + absBounds);
                }
                notifyFocusGained(mFieldTextItem.id, absBounds);
            } else {
                if (DEBUG) Log.d(TAG, "focus lost on " + mFieldTextItem.id);
                notifyFocusLost(mFieldTextItem.id);
            }
        }

        private Rect getAbsCoordinates() {
            // Must offset the boundaries so they're relative to the CustomView.
            int[] offset = new int[2];
            getLocationOnScreen(offset);
            Rect absBounds = new Rect(mBounds.left + offset[0],
                    mBounds.top + offset[1],
                    mBounds.right + offset[0], mBounds.bottom + offset[1]);
            if (VERBOSE) {
                Log.v(TAG, "getAbsCoordinates() for " + mFieldTextItem.id + ": bounds=" + mBounds
                        + " offset: " + Arrays.toString(offset) + " absBounds: " + absBounds);
            }
            return absBounds;
        }

        /**
         * Gets the value of the input field text.
         */
        public CharSequence getText() {
            return mFieldTextItem.text;
        }

        /**
         * Resets the value of the input field text.
         */
        public void reset() {
            mFieldTextItem.text = "        ";
        }

        @Override
        public String toString() {
            return "Label: " + mLabelItem + " Text: " + mFieldTextItem
                    + " Focused: " + mFieldTextItem.focused + " Type: " + mAutofillType;
        }
    }
}
