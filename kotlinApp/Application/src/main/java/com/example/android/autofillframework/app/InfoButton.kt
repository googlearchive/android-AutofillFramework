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
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import com.example.android.autofillframework.R

class InfoButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.InfoButton,
                defStyleAttr, 0)
        val infoText = typedArray.getString(R.styleable.InfoButton_dialogText)
        typedArray.recycle()
        setInfoText(infoText)
    }

    fun setInfoText(infoText: String) {
        setOnClickListener {
            AlertDialog.Builder(this@InfoButton.context).setMessage(infoText).create().show()
        }
    }
}