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
package com.example.android.autofillframework.multidatasetservice.settings

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.example.android.autofillframework.R
import com.example.android.autofillframework.multidatasetservice.datasource.SharedPrefsAutofillRepository
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_credentials_container
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_credentials_icon
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_credentials_label
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_datasets_container
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_datasets_label
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_datasets_switch
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_responses_container
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_responses_label
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_auth_responses_switch
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_clear_data_container
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_clear_data_icon
import kotlinx.android.synthetic.main.multidataset_service_settings_activity.settings_clear_data_label

class SettingsActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.multidataset_service_settings_activity)
        setupSettingsSwitch(settings_auth_responses_container,
                settings_auth_responses_label,
                settings_auth_responses_switch,
                MyPreferences.isResponseAuth(this),
                CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                    MyPreferences.setResponseAuth(this@SettingsActivity, b)
                })
        setupSettingsSwitch(settings_auth_datasets_container,
                settings_auth_datasets_label,
                settings_auth_datasets_switch,
                MyPreferences.isDatasetAuth(this),
                CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                    MyPreferences.setDatasetAuth(this@SettingsActivity, b)
                })
        setupSettingsButton(settings_clear_data_container,
                settings_clear_data_label,
                settings_clear_data_icon,
                View.OnClickListener { buildClearDataDialog().show() })

        setupSettingsButton(settings_auth_credentials_container,
                settings_auth_credentials_label,
                settings_auth_credentials_icon,
                View.OnClickListener {
                    if (MyPreferences.getMasterPassword(this@SettingsActivity) != null) {
                        buildCurrentCredentialsDialog().show()
                    } else {
                        buildNewCredentialsDialog().show()
                    }
                })
    }

    private fun buildClearDataDialog(): AlertDialog {
        return AlertDialog.Builder(this@SettingsActivity)
                .setMessage(R.string.settings_clear_data_confirmation)
                .setTitle(R.string.settings_clear_data_confirmation_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    SharedPrefsAutofillRepository.clear(this@SettingsActivity)
                    MyPreferences.clearCredentials(this@SettingsActivity)
                    dialog.dismiss()
                }
                .create()
    }

    private fun prepareCredentialsDialog(): AlertDialog.Builder {
        return AlertDialog.Builder(this@SettingsActivity)
                .setTitle(R.string.settings_auth_change_credentials_title)
                .setNegativeButton(R.string.cancel, null)
    }

    private fun buildCurrentCredentialsDialog(): AlertDialog {
        val currentPasswordField = LayoutInflater
                .from(this@SettingsActivity)
                .inflate(R.layout.multidataset_service_settings_authentication_dialog, null)
                .findViewById<EditText>(R.id.master_password_field)
        return prepareCredentialsDialog()
                .setMessage(R.string.settings_auth_enter_current_password)
                .setView(currentPasswordField)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    val password = currentPasswordField.text.toString()
                    if (MyPreferences.getMasterPassword(this@SettingsActivity) == password) {
                        buildNewCredentialsDialog().show()
                        dialog.dismiss()
                    }
                }
                .create()
    }

    private fun buildNewCredentialsDialog(): AlertDialog {
        val newPasswordField = LayoutInflater
                .from(this@SettingsActivity)
                .inflate(R.layout.multidataset_service_settings_authentication_dialog, null)
                .findViewById<EditText>(R.id.master_password_field)
        return prepareCredentialsDialog()
                .setMessage(R.string.settings_auth_enter_new_password)
                .setView(newPasswordField)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    val password = newPasswordField.text.toString()
                    MyPreferences.setMasterPassword(this@SettingsActivity, password)
                    dialog.dismiss()
                }
                .create()
    }

    private fun setupSettingsSwitch(container: ViewGroup, switchLabelView: TextView, switchView: Switch, checked: Boolean,
            checkedChangeListener: CompoundButton.OnCheckedChangeListener) {
        val switchLabel = switchLabelView.text.toString()
        with(switchView) {
            contentDescription = switchLabel
            isChecked = checked
            setOnCheckedChangeListener(checkedChangeListener)
        }
        container.setOnClickListener { switchView.performClick() }
    }

    private fun setupSettingsButton(container: ViewGroup, buttonLabelView: TextView, imageView: ImageView,
            onClickListener: View.OnClickListener) {
        val buttonLabel = buttonLabelView.text.toString()
        imageView.contentDescription = buttonLabel
        container.setOnClickListener(onClickListener)
    }
}
