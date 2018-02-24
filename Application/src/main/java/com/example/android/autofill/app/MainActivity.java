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

package com.example.android.autofill.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.android.autofill.app.commoncases.CommonCasesFragment;
import com.example.android.autofill.app.edgecases.EdgeCasesFragment;

/**
 * This is used to launch sample activities that showcase autofill.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * A simple pager adapter that holds 2 Fragments.
     */
    private static class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        private BaseMainFragment[] fragments = new BaseMainFragment[]{new CommonCasesFragment(),
                new EdgeCasesFragment()};

        private Context mContext;

        public ScreenSlidePagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getString(fragments[position].getPageTitleResId());
        }
    }
}