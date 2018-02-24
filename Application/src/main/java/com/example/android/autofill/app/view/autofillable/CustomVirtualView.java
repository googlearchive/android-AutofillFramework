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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
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
import android.widget.Toast;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.Util;
import com.google.common.base.Preconditions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.example.android.autofill.app.Util.bundleToString;

/**
 * A custom View with a virtual structure for fields supporting {@link View#getAutofillHints()}
 */
public class CustomVirtualView extends View {

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

    private static final String TAG = "CustomView";
    private static final int DEFAULT_TEXT_HEIGHT_DP = 34;
    private static final int VERTICAL_GAP = 10;
    private static final int UNFOCUSED_COLOR = Color.BLACK;
    private static final int FOCUSED_COLOR = Color.RED;
    private static int sNextId;
    protected final AutofillManager mAutofillManager;
    private final ArrayList<Line> mVirtualViewGroups = new ArrayList<>();
    private final SparseArray<Item> mVirtualViews = new SparseArray<>();
    private final SparseArray<Partition> mPartitionsByAutofillId = new SparseArray<>();
    private final ArrayMap<String, Partition> mPartitionsByName = new ArrayMap<>();
    protected Line mFocusedLine;
    protected int mTopMargin;
    protected int mLeftMargin;
    private Paint mTextPaint;
    private int mTextHeight;
    private int mLineLength;

    public CustomVirtualView(Context context) {
        this(context, null);
    }

    public CustomVirtualView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVirtualView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomVirtualView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mAutofillManager = context.getSystemService(AutofillManager.class);
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

    protected void resetCoordinates() {
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setTextSize(mTextHeight);
        mTopMargin = getPaddingTop();
        mLeftMargin = getPaddingStart();
        mLineLength = mTextHeight + VERTICAL_GAP;
    }

    @Override
    public void autofill(SparseArray<AutofillValue> values) {
        Context context = getContext();

        // User has just selected a Dataset from the list of autofill suggestions.
        // The Dataset is comprised of a list of AutofillValues, with each AutofillValue meant
        // to fill a specific autofillable view. Now we have to update the UI based on the
        // AutofillValues in the list, but first we make sure all autofilled values belong to the
        // same partition
        if (DEBUG) Log.d(TAG, "autofill(): " + values);

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
            if (DEBUG) Log.d(TAG, "Adding new child at index " + index + ": " + item);
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

    private void showError(String message) {
        showMessage(true, message);
    }

    private void showMessage(String message) {
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
        protected final int id;
        private final String idEntry;
        private final Line line;
        private final boolean editable;
        private final boolean sanitized;
        private final String[] hints;
        private final int type;
        private CharSequence text;
        private boolean focused = false;
        private long date;

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

        public String getClassName() {
            return editable ? EditText.class.getName() : TextView.class.getName();
        }

        public AutofillValue getAutofillValue() {
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
    }

    /**
     * A partition represents a logical group of items, such as credit card info.
     */
    public final class Partition {
        private final String mName;
        private final SparseArray<Line> mLines = new SparseArray<>();

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
            Preconditions.checkArgument(autofillType == AUTOFILL_TYPE_TEXT ||
                    autofillType == AUTOFILL_TYPE_DATE, "Unsupported type: " + autofillType);
            Line line = new Line(idEntryPrefix, autofillType, label, autofillHints, text,
                    !sensitive);
            mVirtualViewGroups.add(line);
            int id = line.mFieldTextItem.id;
            mLines.put(id, line);
            mVirtualViews.put(line.mLabelItem.id, line.mLabelItem);
            mVirtualViews.put(id, line.mFieldTextItem);
            mPartitionsByAutofillId.put(id, this);

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
        private final Rect mBounds = new Rect();
        private final Item mLabelItem;
        private final int mAutofillType;

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
                mAutofillManager.notifyViewEntered(CustomVirtualView.this, mFieldTextItem.id,
                        absBounds);
            } else {
                if (DEBUG) Log.d(TAG, "focus lost on " + mFieldTextItem.id);
                mAutofillManager.notifyViewExited(CustomVirtualView.this, mFieldTextItem.id);
            }
        }

        private Rect getAbsCoordinates() {
            // Must offset the boundaries so they're relative to the CustomView.
            int offset[] = new int[2];
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
            return "Label: " + mLabelItem + " Text: " + mFieldTextItem + " Focused: " +
                    mFieldTextItem.focused + " Type: " + mAutofillType;
        }
    }
}