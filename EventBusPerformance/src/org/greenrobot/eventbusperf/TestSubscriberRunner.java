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

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * This thread initialize all selected tests and runs them through. Also the thread skips the tests, when it is canceled
 */
public class TestSubscriberRunner extends Thread {
    private List<TestSubscriber> testSubscribers;
    private volatile boolean canceled;
    private final EventBus controlBus;

    public TestSubscriberRunner(Context context, TestSubscriberParams testParams, EventBus controlBus) {
        this.controlBus = controlBus;
        testSubscribers = new ArrayList<TestSubscriber>();
        for (Class<? extends TestSubscriber> testClazz : testParams.getTestClasses()) {
            try {
                Constructor<?>[] constructors = testClazz.getConstructors();
                Constructor<? extends TestSubscriber> constructor = testClazz.getConstructor(Context.class, TestSubscriberParams.class);
                TestSubscriber testSubscriber = constructor.newInstance(context, testParams);
                testSubscribers.add(testSubscriber);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void run() {

        int idx = 0;
        for (TestSubscriber testSubscriber : testSubscribers) {
            // Clean up and let the main thread calm down
            System.gc();
            try {
                Thread.sleep(300);
                System.gc();
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }

            testSubscriber.prepareTest();
            if (!canceled) {
                testSubscriber.runTest();
            }
            if (!canceled) {
                boolean isLastEvent = idx == testSubscribers.size() - 1;
                controlBus.post(new TestFinishedEvent(testSubscriber, isLastEvent));
            }
            idx++;
        }

    }

    public List<TestSubscriber> getTestSubscribers() {
        return testSubscribers;
    }

    public void cancel() {
        canceled = true;
        for (TestSubscriber testSubscriber : testSubscribers) {
            testSubscriber.cancel();
        }
    }
}
