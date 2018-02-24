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
package com.example.android.autofill.app.edgecases;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.autofill.app.R;

import java.util.Map;

import static com.example.android.autofill.app.Util.DEBUG;
import static com.example.android.autofill.app.Util.TAG;

/**
 * Activity that emulates a multiple-steps wizard activity, where each step shows just one
 * label and input.
 * <p>
 * <p>Its's useful to verify how an autofill service handles account creation that takes multiple
 * steps.
 */

/*
 * TODO list
 * - use ConstraintLayout
 * - use Fragments instead of replacing views directly
 * - use custom view and/or layout xml for mSteps
 */
abstract class AbstractMultipleStepsActivity extends AppCompatActivity {

    private TextView mStatus;
    private ViewGroup mContainer;

    private Button mPrev;
    private Button mNext;
    private Button mFinish;

    private int mCurrentStep;
    private boolean mFinished;

    private LinearLayout[] mSteps;

    /**
     * Gets the mapping from resource id to autofill hints.
     */
    protected abstract Map<Integer, String> getStepsMap();

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.multiple_steps_activity);

        mStatus = findViewById(R.id.status);
        mContainer = findViewById(R.id.container);
        mPrev = findViewById(R.id.prev);
        mNext = findViewById(R.id.next);
        mFinish = findViewById(R.id.finish);

        View.OnClickListener onClickListener = (v) -> {
            if (v == mPrev) {
                showStep(mCurrentStep - 1);
            } else if (v == mNext) {
                showStep(mCurrentStep + 1);
            } else {
                finishIt();
            }
        };
        mPrev.setOnClickListener(onClickListener);
        mNext.setOnClickListener(onClickListener);
        mFinish.setOnClickListener(onClickListener);

        Map<Integer, String> stepsMap = getStepsMap();
        if (DEBUG) debug("onCreate(): steps=%s", stepsMap);
        initializeSteps(stepsMap);

        showStep(0);
    }

    private void showStep(int i) {
        if (mFinished || i < 0 || i >= mSteps.length) {
            warn("Invalid step: %d (finished=%s, range=[%d,%d])",
                    mFinished, i, 0, mSteps.length - 1);
            return;
        }
        View step = mSteps[i];
        mStatus.setText(getString(R.string.message_showing_step, i));
        if (DEBUG) debug("Showing step %d", i);
        if (mContainer.getChildCount() > 0) {
            mContainer.removeViewAt(0);
        }
        mContainer.addView(step);
        mCurrentStep = i;

        mPrev.setEnabled(mCurrentStep != 0);
        mNext.setEnabled(mCurrentStep != mSteps.length - 1);
    }

    private void updateButtons() {
        mPrev.setEnabled(!mFinished && mCurrentStep != 0);
        mNext.setEnabled(!mFinished && mCurrentStep != mSteps.length - 1);
        mFinish.setEnabled(!mFinished);
    }

    private void finishIt() {
        StringBuilder message = new StringBuilder(getString(R.string.message_finished))
                .append("\n\n");
        for (int i = 0; i < mSteps.length; i++) {
            TextView label = (TextView) mSteps[i].getChildAt(0);
            EditText input = (EditText) mSteps[i].getChildAt(1);
            message.append(getString(R.string.message_step_description, label.getText(), input.getText()))
                    .append('\n');
        }
        mStatus.setText(message.toString());
        mContainer.removeAllViews();
        mFinished = true;
        updateButtons();
    }

    private void initializeSteps(Map<Integer, String> stepsMap) {
        mSteps = new LinearLayout[stepsMap.size()];
        int i = 0;
        for (Map.Entry<Integer, String> entry : stepsMap.entrySet()) {
            int labelId = entry.getKey();
            String autofillHints = entry.getValue();
            if (DEBUG) debug("step %d: %s->%s", i, getString(labelId), autofillHints);
            mSteps[i++] = newStep(labelId, autofillHints);
        }
    }

    private LinearLayout newStep(int labelId, String autofillHints) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(this);
        label.setText(labelId);
        layout.addView(label);

        EditText input = new EditText(this);
        input.setAutofillHints(autofillHints);
        input.setWidth(500); // TODO: proper size
        layout.addView(input);

        return layout;
    }

    protected void debug(String fmt, Object... args) {
        Log.d(TAG, getLocalClassName() + "." + String.format(fmt, args));
    }

    protected void warn(String fmt, Object... args) {
        Log.w(TAG, getLocalClassName() + "." + String.format(fmt, args));
    }
}
