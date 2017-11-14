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
package com.example.android.autofill.app.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;

import com.example.android.autofill.app.R;

public class InfoButton extends AppCompatImageButton {
    public InfoButton(Context context) {
        this(context, null);
    }

    public InfoButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.InfoButton,
                defStyleAttr, 0);
        String infoText = typedArray.getString(R.styleable.InfoButton_dialogText);
        typedArray.recycle();
        setInfoText(infoText);
    }

    public void setInfoText(final String infoText) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(InfoButton.this.getContext())
                        .setMessage(infoText).create().show();
            }
        });
    }
}
