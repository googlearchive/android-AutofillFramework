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

package com.example.android.autofill.app.commoncases;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.WelcomeActivity;

import java.util.Arrays;
import java.util.List;

/**
 * This is mostly a normal Activity containing a RecyclerView. The only difference is, the rows in
 * the RecyclerView have autofillable fields. Therefore, when we bind data to a recycled view, we
 * need to also set the {@link AutofillId} on the view.
 */
@RequiresApi(28)
public class RecyclerViewActivity extends AppCompatActivity {
    private AutofillManager mAfm;
    private List<FieldMetadata> mFields;
    private FieldAdapter mFieldAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_activity);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mAfm = getSystemService(AutofillManager.class);

        // Init RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        mFields = initList();
        mFieldAdapter = new FieldAdapter(mFields);
        recyclerView.setAdapter(mFieldAdapter);
        recyclerView.setLayoutManager(layoutManager);

        // Init submit and clear buttons.
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutofillManager afm = getSystemService(AutofillManager.class);
                if (afm != null) {
                    afm.cancel();
                }
                resetFields();
            }
        });
    }

    private void resetFields() {
        for (FieldMetadata fieldMetadata : mFields) {
            fieldMetadata.setEnteredText("");
        }
        mFieldAdapter.notifyDataSetChanged();
    }

    private void submit() {
        Intent intent = WelcomeActivity.getStartActivityIntent(RecyclerViewActivity.this);
        startActivity(intent);
        finish();
    }

    public List<FieldMetadata> initList() {
        return Arrays.asList(
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        View.AUTOFILL_HINT_NAME,
                        R.string.recycler_view_label_name,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_TEXT
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        "bday-month",
                        R.string.recycler_view_label_birthday_month,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_NUMBER
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        View.AUTOFILL_HINT_EMAIL_ADDRESS,
                        R.string.recycler_view_label_email,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        View.AUTOFILL_HINT_PHONE,
                        R.string.recycler_view_label_phone,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_PHONE
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        "tel_extension",
                        R.string.recycler_view_label_tel_extension,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_PHONE
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
                        R.string.recycler_view_label_cc_number,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_NUMBER
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE,
                        R.string.recycler_view_label_cc_sc,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_NUMBER
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,
                        R.string.recycler_view_label_cc_exp_month,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_NUMBER
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
                        R.string.recycler_view_label_cc_exp_year,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_NUMBER
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        "address-line1",
                        R.string.recycler_view_label_address_line_1,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        "address-line2",
                        R.string.recycler_view_label_address_line_2,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        "address-line3",
                        R.string.recycler_view_label_address_line_3,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        View.AUTOFILL_HINT_POSTAL_CODE,
                        R.string.recycler_view_label_postal_code,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_NUMBER
                ),
                new FieldMetadata(
                        mAfm.getNextAutofillId(),
                        "bday-year",
                        R.string.recycler_view_label_birthday_year,
                        R.drawable.ic_person_black_24dp,
                        InputType.TYPE_CLASS_NUMBER
                )
        );
    }

    static class FieldAdapter extends ListAdapter<FieldMetadata, FieldViewHolder> {
        private List<FieldMetadata> mFields;
        public FieldAdapter(List<FieldMetadata> fields) {
            super(new FieldDiff());
            mFields = fields;
            submitList(mFields);
        }

        @NonNull
        @Override
        public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FieldViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_data_field, parent, false),
                    new FieldWatcher(mFields));
        }

        @Override
        public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
            holder.bind(getItem(position));
        }
    }

    static class FieldViewHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        TextView mLabel;
        EditText mField;
        FieldWatcher mWatcher;

        public FieldViewHolder(@NonNull View itemView, FieldWatcher textWatcher) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.icon);
            mLabel = itemView.findViewById(R.id.label);
            mField = itemView.findViewById(R.id.field);
            mWatcher = textWatcher;
            mField.addTextChangedListener(mWatcher);
        }

        void bind(FieldMetadata fieldMetadata) {
            mWatcher.updatePosition(getAdapterPosition());
            Drawable drawable = mIcon.getResources().getDrawable(fieldMetadata.getIconRes());
            mIcon.setImageDrawable(drawable);
            mLabel.setText(fieldMetadata.getLabelRes());
            mField.setAutofillHints(fieldMetadata.getAutofillHint());
            mField.setInputType(fieldMetadata.getInputType());
            mField.setText(fieldMetadata.getEnteredText());

            // IMPORTANT: setAutofillId of recycled View.
            mField.setAutofillId(fieldMetadata.getAutofillId());
        }
    }

    /**
     * TextWatcher implementation to ensure EditTexts get recycled properly.
     */
    static class FieldWatcher implements TextWatcher {
        private int mPosition;
        private List<FieldMetadata> mFields;

        public FieldWatcher(List<FieldMetadata> fields) {
            mFields = fields;
        }

        public void updatePosition(int position) {
            this.mPosition = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // NO-OP
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mFields.get(mPosition).setEnteredText(charSequence);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // NO-OP
        }
    }

    /**
     * Model class that holds all of the data needed for a row in the {@link RecyclerView}.
     */
    static class FieldMetadata {
        AutofillId mAutofillId;
        String mAutofillHint;
        @StringRes int mLabelRes;
        @DrawableRes int mIconRes;
        int mInputType;
        CharSequence mEnteredText = "";

        FieldMetadata(AutofillId autofillId, String autofillHint, @StringRes int labelRes,
                @DrawableRes int iconRes, int inputType) {
            mAutofillId = autofillId;
            mAutofillHint = autofillHint;
            mLabelRes = labelRes;
            mIconRes = iconRes;
            mInputType = inputType;
        }

        public AutofillId getAutofillId() {
            return mAutofillId;
        }

        public String getAutofillHint() {
            return mAutofillHint;
        }

        public int getLabelRes() {
            return mLabelRes;
        }

        public int getIconRes() {
            return mIconRes;
        }

        public int getInputType() {
            return mInputType;
        }

        public void setEnteredText(CharSequence enteredText) {
            mEnteredText = enteredText;
        }

        public CharSequence getEnteredText() {
            return mEnteredText;
        }
    }

    static class FieldDiff extends DiffUtil.ItemCallback<FieldMetadata> {
        @Override
        public boolean areItemsTheSame(@NonNull FieldMetadata oldItem,
                @NonNull FieldMetadata newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull FieldMetadata oldItem,
                @NonNull FieldMetadata newItem) {
            return oldItem.equals(newItem);
        }
    }
}
