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
import android.graphics.PorterDuff
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.navigation_item.view.buttonLabel
import kotlinx.android.synthetic.main.navigation_item.view.cardView
import kotlinx.android.synthetic.main.navigation_item.view.infoButton

class NavigationItem @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigationItem,
                defStyleAttr, 0)
        val labelText = typedArray.getString(R.styleable.NavigationItem_labelText)
        val infoText = typedArray.getString(R.styleable.NavigationItem_infoText)
        val logoDrawable = typedArray.getDrawable(R.styleable.NavigationItem_itemLogo)
        @ColorRes val colorRes = typedArray.getResourceId(R.styleable.NavigationItem_imageColor, 0)
        val imageColor = ContextCompat.getColor(getContext(), colorRes)
        typedArray.recycle()
        LayoutInflater.from(context).inflate(R.layout.navigation_item, this)
        logoDrawable?.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
        buttonLabel.apply {
            text = labelText
            setCompoundDrawablesRelativeWithIntrinsicBounds(logoDrawable, null, null, null)
        }
        infoButton.apply {
            setOnClickListener {
                AlertDialog.Builder(this@NavigationItem.context)
                        .setMessage(infoText).create().show()
            }
            setColorFilter(imageColor)
        }
    }

    fun setNavigationButtonClickListener(l: View.OnClickListener?) {
        cardView.setOnClickListener(l)
    }
}
