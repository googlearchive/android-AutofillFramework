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
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.autofill.app.R;

import static com.example.android.autofill.app.Util.TAG;

public class NavigationItem extends FrameLayout {
    public NavigationItem(Context context) {
        this(context, null);
    }

    public NavigationItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationItem(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NavigationItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigationItem,
                defStyleAttr, defStyleRes);
        String labelText = typedArray.getString(R.styleable.NavigationItem_labelText);
        String infoText = typedArray.getString(R.styleable.NavigationItem_infoText);
        Drawable logoDrawable = typedArray.getDrawable(R.styleable.NavigationItem_itemLogo);
        @ColorRes int colorRes = typedArray.getResourceId(R.styleable.NavigationItem_imageColor, 0);
        String launchingActivityName = typedArray.getString(R.styleable.NavigationItem_destinationActivityName);
        int imageColor = ContextCompat.getColor(getContext(), colorRes);
        typedArray.recycle();
        View rootView = LayoutInflater.from(context).inflate(R.layout.navigation_item, this);
        TextView buttonLabel = rootView.findViewById(R.id.buttonLabel);
        buttonLabel.setText(labelText);
        if (logoDrawable != null) {
            Drawable mutatedLogoDrawable = logoDrawable.mutate();
            mutatedLogoDrawable.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN);
            buttonLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(mutatedLogoDrawable, null,
                    null, null);
        }
        InfoButton infoButton = rootView.findViewById(R.id.infoButton);
        infoButton.setInfoText(infoText);
        infoButton.setColorFilter(imageColor);
        CardView outerView = rootView.findViewById(R.id.cardView);
        outerView.setOnClickListener((view) -> {
            if (launchingActivityName != null) {
                Intent intent = new Intent();
                intent.setClassName(getContext().getPackageName(), launchingActivityName);
                context.startActivity(intent);
            } else {
                Log.w(TAG, "Launching Activity name not set.");
            }
        });
    }
}
