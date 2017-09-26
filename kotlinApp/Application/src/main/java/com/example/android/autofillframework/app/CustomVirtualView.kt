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
import com.example.android.autofillframework.CommonUtil.TAG
import com.example.android.autofillframework.CommonUtil.bundleToString
import com.example.android.autofillframework.R
import java.util.Arrays


/**
 * Custom View with virtual child views for Username/Password text fields.
 */
class CustomVirtualView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    val usernameText: CharSequence
        get() = usernameLine.fieldTextItem.text
    val passwordText: CharSequence
        get() = passwordLine.fieldTextItem.text
    private var nextId: Int = 0
    private val lines = ArrayList<Line>()
    private val items = SparseArray<Item>()
    private val autofillManager = context.getSystemService(AutofillManager::class.java)
    private var focusedLine: Line? = null
    private val textHeight = 90
    private val textPaint = Paint().apply {
        style = Style.FILL
        textSize = textHeight.toFloat()
    }
    private val topMargin = 100
    private val leftMargin = 100
    private val verticalGap = 10
    private val lineLength = textHeight + verticalGap
    private val focusedColor = Color.RED
    private val unfocusedColor = Color.BLACK
    private val usernameLine = addLine("usernameField", context.getString(R.string.username_label),
            arrayOf(View.AUTOFILL_HINT_USERNAME), "         ", true)
    private val passwordLine = addLine("passwordField", context.getString(R.string.password_label),
            arrayOf(View.AUTOFILL_HINT_PASSWORD), "         ", false)

    override fun autofill(values: SparseArray<AutofillValue>) {
        // User has just selected a Dataset from the list of autofill suggestions.
        // The Dataset is comprised of a list of AutofillValues, with each AutofillValue meant
        // to fill a specific autofillable view. Now we have to update the UI based on the
        // AutofillValues in the list.
        Log.d(TAG, "autofill(): " + values)
        for (i in 0 until values.size()) {
            val id = values.keyAt(i)
            val value = values.valueAt(i)
            items[id]?.apply {
                if (editable) {
                    // Set the item's text to the text wrapped in the AutofillValue.
                    text = value.textValue
                } else {
                    Log.w(TAG, "Item for autofillId $id is not editable: $this")
                }
            }
        }
        postInvalidate()
    }

    override fun onProvideAutofillVirtualStructure(structure: ViewStructure, flags: Int) {
        // Build a ViewStructure to pack in AutoFillService requests.
        structure.setClassName(javaClass.name)
        val childrenSize = items.size()
        Log.d(TAG, "onProvideAutofillVirtualStructure(): flags = " + flags + ", items = "
                + childrenSize + ", extras: " + bundleToString(structure.extras))
        var index = structure.addChildCount(childrenSize)
        for (i in 0 until childrenSize) {
            val item = items.valueAt(i)
            Log.d(TAG, "Adding new child at index $index: $item")
            structure.newChild(index).apply {
                setAutofillId(structure.autofillId, item.id)
                setAutofillHints(item.hints)
                setAutofillType(item.type)
                setDataIsSensitive(!item.sanitized)
                setAutofillValue(AutofillValue.forText(item.text))
                setFocused(item.focused)
                setId(item.id, context.packageName, null, item.line.idEntry)
                setClassName(item.className)
            }
            index++
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.d(TAG, "onDraw: " + lines.size + " lines; canvas:" + canvas)
        var x: Float
        var y = (topMargin + lineLength).toFloat()

        lines.forEach {
            x = leftMargin.toFloat()
            Log.v(TAG, "Drawing $it at x=$x, y=$y")
            textPaint.color = if (it.fieldTextItem.focused) focusedColor else unfocusedColor
            val readOnlyText = it.labelItem.text.toString() + ":  ["
            val writeText = it.fieldTextItem.text.toString() + "]"
            // Paints the label first...
            canvas.drawText(readOnlyText, x, y, textPaint)
            // ...then paints the edit text and sets the proper boundary
            val deltaX = textPaint.measureText(readOnlyText)
            x += deltaX
            it.bounds.set(x.toInt(), (y - lineLength).toInt(),
                    (x + textPaint.measureText(writeText)).toInt(), y.toInt())
            Log.d(TAG, "setBounds(" + x + ", " + y + "): " + it.bounds)
            canvas.drawText(writeText, x, y, textPaint)
            y += lineLength.toFloat()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val y = event.y.toInt()
        Log.d(TAG, "Touched: y=$y, range=$lineLength, top=$topMargin")
        var lowerY = topMargin
        var upperY = -1
        for (line in lines) {
            upperY = lowerY + lineLength
            Log.d(TAG, "Line $line ranges from $lowerY to $upperY")
            if (y in lowerY..upperY) {
                Log.d(TAG, "Removing focus from " + focusedLine)
                focusedLine?.changeFocus(false)
                Log.d(TAG, "Changing focus to " + line)
                focusedLine = line.apply { changeFocus(true) }
                invalidate()
                break
            }
            lowerY += lineLength
        }
        return super.onTouchEvent(event)
    }

    fun resetFields() {
        usernameLine.reset()
        passwordLine.reset()
        postInvalidate()
    }

    private fun addLine(idEntry: String, label: String, hints: Array<String>, text: String,
            sanitized: Boolean) = Line(idEntry, label, hints, text, sanitized).also {
        lines.add(it)
        items.apply {
            put(it.labelItem.id, it.labelItem)
            put(it.fieldTextItem.id, it.fieldTextItem)
        }
    }

    private inner class Item internal constructor(
            val line: Line,
            val id: Int,
            val hints: Array<String>?,
            val type: Int, var text: CharSequence, val editable: Boolean,
            val sanitized: Boolean) {
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
                autofillManager.notifyViewEntered(this@CustomVirtualView, fieldTextItem.id, absBounds)
            } else {
                Log.d(TAG, "focus lost on " + fieldTextItem.id)
                autofillManager.notifyViewExited(this@CustomVirtualView, fieldTextItem.id)
            }
        }

        private val absCoordinates: Rect
                // Must offset the boundaries so they're relative to the CustomView.
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
}