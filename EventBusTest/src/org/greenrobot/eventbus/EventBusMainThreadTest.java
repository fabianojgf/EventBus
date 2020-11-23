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
package org.greenrobot.eventbus;

import android.os.Looper;

import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusMainThreadTest extends AbstractAndroidEventBusTest {

    @Test
    public void testPost() throws InterruptedException {
        eventBus.registerSubscriber(this);
        eventBus.post("Hello");
        waitForEventCount(1, 1000);

        assertEquals("Hello", lastEvent);
        assertEquals(Looper.getMainLooper().getThread(), lastThread);
    }

    @Test
    public void testThrow() throws InterruptedException {
        eventBus.registerHandler(this);
        eventBus.throwException("Hello");
        waitForExceptionalEventCount(1, 1000);

        assertEquals("Hello", lastExceptionalEvent);
        assertEquals(Looper.getMainLooper().getThread(), lastExceptionalThread);
    }

    @Test
    public void testPostInBackgroundThread() throws InterruptedException {
        TestBackgroundPoster backgroundPoster = new TestBackgroundPoster(eventBus);
        backgroundPoster.start();

        eventBus.registerSubscriber(this);
        backgroundPoster.post("Hello");
        waitForEventCount(1, 1000);
        assertEquals("Hello", lastEvent);
        assertEquals(Looper.getMainLooper().getThread(), lastThread);

        backgroundPoster.shutdown();
        backgroundPoster.join();
    }

    @Test
    public void testThrowInBackgroundThread() throws InterruptedException {
        TestBackgroundThrower backgroundThrower = new TestBackgroundThrower(eventBus);
        backgroundThrower.start();

        eventBus.registerHandler(this);
        backgroundThrower.throwException("Hello");
        waitForExceptionalEventCount(1, 1000);
        assertEquals("Hello", lastExceptionalEvent);
        assertEquals(Looper.getMainLooper().getThread(), lastExceptionalThread);

        backgroundThrower.shutdown();
        backgroundThrower.join();
    }

    /** Common flow */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String event) {
        trackEvent(event);
    }

    /** Exceptional flow */

    @Handle(threadMode = ExceptionalThreadMode.MAIN)
    public void onExceptionalEventMainThread(String exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
    }
}
