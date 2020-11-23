/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
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

package org.greenrobot.eventbusperf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import org.greenrobot.eventbusperf.testsubject.PerfTestSubscriberEventBus;
import org.greenrobot.eventbusperf.testsubject.PerfTestSubscriberOtto;

public class TestSetupSubscriberActivity extends Activity {

    @SuppressWarnings("rawtypes")
    static final Class[] TEST_CLASSES_EVENTBUS = {PerfTestSubscriberEventBus.Post.class,//
            PerfTestSubscriberEventBus.RegisterOneByOne.class,//
            PerfTestSubscriberEventBus.RegisterAll.class, //
            PerfTestSubscriberEventBus.RegisterFirstTime.class};

    static final Class[] TEST_CLASSES_OTTO = {PerfTestSubscriberOtto.Post.class,//
            PerfTestSubscriberOtto.RegisterOneByOne.class,//
            PerfTestSubscriberOtto.RegisterAll.class, //
            PerfTestSubscriberOtto.RegisterFirstTime.class};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setuptests);

        Spinner spinnerRun = findViewById(R.id.spinnerTestToRun);
        spinnerRun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapter, View v, int pos, long lng) {
                int eventsVisibility = pos == 0 ? View.VISIBLE : View.GONE;
                findViewById(R.id.relativeLayoutForEvents).setVisibility(eventsVisibility);
                findViewById(R.id.spinnerThread).setVisibility(eventsVisibility);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void checkEventBus(View v) {
        Spinner spinnerThread = findViewById(R.id.spinnerThread);
        CheckBox checkBoxEventBus = findViewById(R.id.checkBoxEventBus);
        int visibility = checkBoxEventBus.isChecked() ? View.VISIBLE : View.GONE;
        spinnerThread.setVisibility(visibility);
    }

    public void startClick(View v) {
        TestSubscriberParams params = new TestSubscriberParams();
        Spinner spinnerThread = findViewById(R.id.spinnerThread);
        String threadModeStr = spinnerThread.getSelectedItem().toString();
        ThreadMode threadMode = ThreadMode.valueOf(threadModeStr);
        params.setThreadMode(threadMode);

        params.setEventInheritance(((CheckBox) findViewById(R.id.checkBoxEventBusEventHierarchy)).isChecked());
        params.setIgnoreGeneratedIndex(((CheckBox) findViewById(R.id.checkBoxEventBusIgnoreGeneratedIndex)).isChecked());

        EditText editTextEvent = findViewById(R.id.editTextEvent);
        params.setEventCount(Integer.parseInt(editTextEvent.getText().toString()));

        EditText editTextSubscriber = findViewById(R.id.editTextSubscribe);
        params.setSubscriberCount(Integer.parseInt(editTextSubscriber.getText().toString()));

        Spinner spinnerTestToRun = findViewById(R.id.spinnerTestToRun);
        int testPos = spinnerTestToRun.getSelectedItemPosition();
        params.setTestNumber(testPos + 1);
        ArrayList<Class<? extends TestSubscriber>> testClasses = initTestClasses(testPos);
        params.setTestClasses(testClasses);

        Intent intent = new Intent();
        intent.setClass(this, TestRunnerSubscriberActivity.class);
        intent.putExtra("params", params);
        startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Class<? extends TestSubscriber>> initTestClasses(int testPos) {
        ArrayList<Class<? extends TestSubscriber>> testClasses = new ArrayList<Class<? extends TestSubscriber>>();
        // the attributes are putted in the intent (eventbus, otto, broadcast, local broadcast)
        final CheckBox checkBoxEventBus = findViewById(R.id.checkBoxEventBus);
        final CheckBox checkBoxOtto = findViewById(R.id.checkBoxOtto);
        final CheckBox checkBoxBroadcast = findViewById(R.id.checkBoxBroadcast);
        final CheckBox checkBoxLocalBroadcast = findViewById(R.id.checkBoxLocalBroadcast);
        if (checkBoxEventBus.isChecked()) {
            testClasses.add(TEST_CLASSES_EVENTBUS[testPos]);
        }
        if (checkBoxOtto.isChecked()) {
            testClasses.add(TEST_CLASSES_OTTO[testPos]);
        }
        if (checkBoxBroadcast.isChecked()) {
        }
        if (checkBoxLocalBroadcast.isChecked()) {
        }

        return testClasses;
    }
}