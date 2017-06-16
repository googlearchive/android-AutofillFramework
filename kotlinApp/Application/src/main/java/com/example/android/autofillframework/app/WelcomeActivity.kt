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
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.welcome_activity.countdownText
import java.lang.Math.toIntExact


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_activity)
        object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = toIntExact(millisUntilFinished / 1000)
                countdownText.text = getResources()
                        .getQuantityString(R.plurals.welcome_page_countdown, secondsRemaining,
                                secondsRemaining)
            }

            override fun onFinish() {
                if (!this@WelcomeActivity.isFinishing) {
                    finish()
                }
            }

        }.start()
    }

    companion object {

        fun getStartActivityIntent(context: Context): Intent {
            return Intent(context, WelcomeActivity::class.java)
        }
    }
}
