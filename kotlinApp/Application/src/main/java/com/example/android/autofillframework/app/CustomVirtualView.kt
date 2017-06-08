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
package com.example.android.autofillframework.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewStructure
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.EditText
import android.widget.TextView
import com.example.android.autofillframework.CommonUtil.bundleToString
import com.example.android.autofillframework.R
import java.util.Arrays


/**
 * Custom View with virtual child views for Username/Password text fields.
 */
class CustomVirtualView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val mLines = ArrayList<Line>()
    private val mItems = SparseArray<Item>()
    private val mAfm: AutofillManager = context.getSystemService(AutofillManager::class.java)

    private var mFocusedLine: Line? = null
    private val mTextPaint: Paint = Paint()
    private val mTextHeight: Int = 90
    private val mTopMargin: Int = 100
    private val mLeftMargin: Int = 100
    private val mVerticalGap: Int = 10
    private val mLineLength: Int = mTextHeight + mVerticalGap
    private val mFocusedColor: Int = Color.RED
    private val mUnfocusedColor: Int = Color.BLACK

    private val mUsernameLine: Line
    private val mPasswordLine: Line

    init {
        mTextPaint.style = Style.FILL
        mTextPaint.textSize = mTextHeight.toFloat()
        mUsernameLine = addLine("usernameField", context.getString(R.string.username_label),
                arrayOf(View.AUTOFILL_HINT_USERNAME), "         ", true)
        mPasswordLine = addLine("passwordField", context.getString(R.string.password_label),
                arrayOf(View.AUTOFILL_HINT_PASSWORD), "         ", false)

        Log.d(TAG, "Text height: " + mTextHeight)
    }

    override fun autofill(values: SparseArray<AutofillValue>) {
        // User has just selected a Dataset from the list of autofill suggestions.
        // The Dataset is comprised of a list of AutofillValues, with each AutofillValue meant
        // to fill a specific autofillable view. Now we have to update the UI based on the
        // AutofillValues in the list.
        Log.d(TAG, "autofill(): " + values)
        for (i in 0..values.size() - 1) {
            val id = values.keyAt(i)
            val value = values.valueAt(i)
            mItems[id]?.let { item ->
                if (item.editable) {
                    // Set the item's text to the text wrapped in the AutofillValue.
                    item.text = value.textValue
                } else {
                    Log.w(TAG, "Item for autofillId $id is not editable: $item")
                }
            }
        }
        postInvalidate()
    }

    override fun onProvideAutofillVirtualStructure(structure: ViewStructure, flags: Int) {
        // Build a ViewStructure to pack in AutoFillService requests.
        structure.setClassName(javaClass.name)
        val childrenSize = mItems.size()
        Log.d(TAG, "onProvideAutofillVirtualStructure(): flags = " + flags + ", items = "
                + childrenSize + ", extras: " + bundleToString(structure.extras))
        var index = structure.addChildCount(childrenSize)
        for (i in 0..childrenSize - 1) {
            val item = mItems.valueAt(i)
            Log.d(TAG, "Adding new child at index $index: $item")
            val child = structure.newChild(index)
            child.setAutofillId(structure.getAutofillId(), item.id)
            child.setAutofillHints(item.hints)
            child.setAutofillType(item.type)
            child.setDataIsSensitive(!item.sanitized)
            child.text = item.text
            child.setAutofillValue(AutofillValue.forText(item.text))
            child.setFocused(item.focused)
            child.setId(item.id, context.packageName, null, item.line.idEntry)
            child.setClassName(item.className)
            index++
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.d(TAG, "onDraw: " + mLines.size + " lines; canvas:" + canvas)
        var x: Float
        var y = (mTopMargin + mLineLength).toFloat()
        for (line in mLines) {
            x = mLeftMargin.toFloat()
            Log.v(TAG, "Drawing '" + line + "' at " + x + "x" + y)
            mTextPaint.color = if (line.fieldTextItem.focused) mFocusedColor else mUnfocusedColor
            val readOnlyText = line.labelItem.text.toString() + ":  ["
            val writeText = line.fieldTextItem.text.toString() + "]"
            // Paints the label first...
            canvas.drawText(readOnlyText, x, y, mTextPaint)
            // ...then paints the edit text and sets the proper boundary
            val deltaX = mTextPaint.measureText(readOnlyText)
            x += deltaX
            line.bounds.set(x.toInt(), (y - mLineLength).toInt(),
                    (x + mTextPaint.measureText(writeText)).toInt(), y.toInt())
            Log.d(TAG, "setBounds(" + x + ", " + y + "): " + line.bounds)
            canvas.drawText(writeText, x, y, mTextPaint)
            y += mLineLength.toFloat()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val y = event.y.toInt()
        Log.d(TAG, "Touched: y=$y, range=$mLineLength, top=$mTopMargin")
        var lowerY = mTopMargin
        var upperY = -1
        for (i in mLines.indices) {
            upperY = lowerY + mLineLength
            val line = mLines[i]
            Log.d(TAG, "Line $i ranges from $lowerY to $upperY")
            if (lowerY <= y && y <= upperY) {
                Log.d(TAG, "Removing focus from " + mFocusedLine)
                mFocusedLine?.changeFocus(false)
                Log.d(TAG, "Changing focus to " + line)
                mFocusedLine = line
                mFocusedLine?.changeFocus(true)
                invalidate()
                break
            }
            lowerY += mLineLength
        }
        return super.onTouchEvent(event)
    }

    val usernameText: CharSequence
        get() = mUsernameLine.fieldTextItem.text

    val passwordText: CharSequence
        get() = mPasswordLine.fieldTextItem.text

    fun resetFields() {
        mUsernameLine.reset()
        mPasswordLine.reset()
        postInvalidate()
    }

    private fun addLine(idEntry: String, label: String, hints: Array<String>, text: String, sanitized: Boolean): Line {
        val line = Line(idEntry, label, hints, text, sanitized)
        mLines.add(line)
        mItems.put(line.labelItem.id, line.labelItem)
        mItems.put(line.fieldTextItem.id, line.fieldTextItem)
        return line
    }

    private class Item internal constructor(val line: Line, val id: Int, val hints: Array<String>?,
            val type: Int, var text: CharSequence,
            val editable: Boolean, val sanitized: Boolean) {
        var focused = false

        override fun toString(): String {
            return id.toString() + ": " + text + if (editable)
                " (editable)"
            else
                " (read-only)" + if (sanitized) " (sanitized)" else " (sensitive"
        }

        val className: String
            get() = if (editable) EditText::class.java.name else TextView::class.java.name
    }

    private inner class Line constructor(val idEntry: String, label: String, hints: Array<String>,
            text: String, sanitized: Boolean) {

        // Boundaries of the text field, relative to the CustomView
        internal val bounds = Rect()
        var labelItem: Item = Item(this, ++nextId, null, View.AUTOFILL_TYPE_NONE, label, false, true)
        var fieldTextItem: Item = Item(this, ++nextId, hints, View.AUTOFILL_TYPE_TEXT, text, true, sanitized)

        internal fun changeFocus(focused: Boolean) {
            fieldTextItem.focused = focused
            if (focused) {
                val absBounds = absCoordinates
                Log.d(TAG, "focus gained on " + fieldTextItem.id + "; absBounds=" + absBounds)
                mAfm.notifyViewEntered(this@CustomVirtualView, fieldTextItem.id, absBounds)
            } else {
                Log.d(TAG, "focus lost on " + fieldTextItem.id)
                mAfm.notifyViewExited(this@CustomVirtualView, fieldTextItem.id)
            }
        }

        private // Must offset the boundaries so they're relative to the CustomView.
        val absCoordinates: Rect
            get() {
                val offset = IntArray(2)
                getLocationOnScreen(offset)
                val absBounds = Rect(bounds.left + offset[0],
                        bounds.top + offset[1],
                        bounds.right + offset[0], bounds.bottom + offset[1])
                Log.v(TAG, "absCoordinates for " + fieldTextItem.id + ": bounds=" + bounds
                        + " offset: " + Arrays.toString(offset) + " absBounds: " + absBounds)
                return absBounds
            }

        fun reset() {
            fieldTextItem.text = "        "
        }

        override fun toString(): String {
            return "Label: " + labelItem + " Text: " + fieldTextItem + " Focused: " +
                    fieldTextItem.focused
        }
    }

    companion object {

        private val TAG = "CustomView"

        private var nextId: Int = 0
    }
}