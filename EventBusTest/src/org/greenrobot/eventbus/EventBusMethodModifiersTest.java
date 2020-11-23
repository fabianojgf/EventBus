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

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusMethodModifiersTest extends AbstractAndroidEventBusTest {

    @Test
    public void testRegisterForEventTypeAndPost() throws InterruptedException {
        eventBus.registerSubscriber(this);
        String event = "Hello";
        eventBus.post(event);
        waitForEventCount(4, 1000);
    }

    @Test
    public void testRegisterForExceptionalEventTypeAndThrow() throws InterruptedException {
        eventBus.registerHandler(this);
        String exceptionalEvent = "Hello";
        eventBus.throwException(exceptionalEvent);
        waitForExceptionalEventCount(4, 1000);
    }

    /** Common flow */

    @Subscribe
    public void onEvent(String event) {
        trackEvent(event);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String event) {
        trackEvent(event);
        assertSame(Looper.getMainLooper(), Looper.myLooper());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackgroundThread(String event) {
        trackEvent(event);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsync(String event) {
        trackEvent(event);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }

    /** Exceptional flow */

    @Handle
    public void onExceptionalEvent(String exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }

    @Handle(threadMode = ExceptionalThreadMode.MAIN)
    public void onExceptionalEventMainThread(String exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
        assertSame(Looper.getMainLooper(), Looper.myLooper());
    }

    @Handle(threadMode = ExceptionalThreadMode.BACKGROUND)
    public void onExceptionalEventBackgroundThread(String exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }

    @Handle(threadMode = ExceptionalThreadMode.ASYNC)
    public void onExceptionalEventAsync(String exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }
}
