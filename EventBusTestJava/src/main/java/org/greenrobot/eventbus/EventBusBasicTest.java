/*
 * Copyright (C) 2012-2017 Markus Junginger, greenrobot (http://greenrobot.org)
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

import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Markus Junginger, greenrobot
 */
@SuppressWarnings({"WeakerAccess", "UnusedParameters", "unused"})
public class EventBusBasicTest extends AbstractEventBusTest {

    public static class WithIndex extends EventBusBasicTest {
        @Test
        public void dummy() {
        }

    }

    /** Common flow */
    private String lastStringEvent;
    private int countStringEvent;
    private int countIntEvent;
    private int lastIntEvent;
    private int countMyEventExtended;
    private int countMyEvent;
    private int countMyEvent2;

    /** Exceptional flow */
    private String lastStringExceptionalEvent;
    private int countStringExceptionalEvent;
    private int countIntExceptionalEvent;
    private int lastIntExceptionalEvent;
    private int countMyExceptionalEventExtended;
    private int countMyExceptionalEvent;
    private int countMyExceptionalEvent2;

    @Test
    public void testRegisterAndPost() {
        // Use an activity to test real life performance
        StringEventSubscriber stringEventSubscriber = new StringEventSubscriber();
        String event = "Hello";

        long start = System.currentTimeMillis();
        eventBus.registerSubscriber(stringEventSubscriber);
        long time = System.currentTimeMillis() - start;
        log("Registered in " + time + "ms");

        eventBus.post(event);

        assertEquals(event, stringEventSubscriber.lastStringEvent);
    }

    @Test
    public void testRegisterAndThrow() {
        // Use an activity to test real life performance
        StringExceptionalEventHandler stringExceptionalEventHandler = new StringExceptionalEventHandler();
        String exceptionalEvent = "Hello";

        long start = System.currentTimeMillis();
        eventBus.registerHandler(stringExceptionalEventHandler);
        long time = System.currentTimeMillis() - start;
        log("Registered in " + time + "ms");

        eventBus.throwException(exceptionalEvent);

        assertEquals(exceptionalEvent, stringExceptionalEventHandler.lastStringExceptionalEvent);
    }

    @Test
    public void testPostWithoutSubscriber() {
        eventBus.post("Hello");
    }

    @Test
    public void testThrowWithoutHandler() {
        eventBus.throwException("Hello");
    }

    @Test
    public void testUnregisterSubscriberWithoutRegisterSubscriber() {
        // Results in a warning without throwing
        eventBus.unregisterSubscriber(this);
    }

    @Test
    public void testUnregisterHandlerWithoutRegisterHandler() {
        // Results in a warning without throwing
        eventBus.unregisterHandler(this);
    }

    // This will throw "out of memory" if subscribers are leaked
    @Test
    public void testUnregisterSubscriberNotLeaking() {
        int heapMBytes = (int) (Runtime.getRuntime().maxMemory() / (1024L * 1024L));
        for (int i = 0; i < heapMBytes * 2; i++) {
            @SuppressWarnings("unused")
            EventBusBasicTest subscriber = new EventBusBasicTest() {
                byte[] expensiveObject = new byte[1024 * 1024];
            };
            eventBus.registerSubscriber(subscriber);
            eventBus.unregisterSubscriber(subscriber);
            log("Iteration " + i + " / max heap: " + heapMBytes);
        }
    }

    // This will throw "out of memory" if subscribers are leaked
    @Test
    public void testUnregisterHandlerNotLeaking() {
        int heapMBytes = (int) (Runtime.getRuntime().maxMemory() / (1024L * 1024L));
        for (int i = 0; i < heapMBytes * 2; i++) {
            @SuppressWarnings("unused")
            EventBusBasicTest handler = new EventBusBasicTest() {
                byte[] expensiveObject = new byte[1024 * 1024];
            };
            eventBus.registerHandler(handler);
            eventBus.unregisterHandler(handler);
            log("Iteration " + i + " / max heap: " + heapMBytes);
        }
    }

    @Test
    public void testRegisterSubscriberTwice() {
        eventBus.registerSubscriber(this);
        try {
            eventBus.registerSubscriber(this);
            fail("Did not throw");
        } catch (RuntimeException expected) {
            // OK
        }
    }

    @Test
    public void testRegisterHandlerTwice() {
        eventBus.registerHandler(this);
        try {
            eventBus.registerHandler(this);
            fail("Did not throw");
        } catch (RuntimeException expected) {
            // OK
        }
    }

    @Test
    public void testIsRegisteredSubscriber() {
        assertFalse(eventBus.isRegisteredSubscriber(this));
        eventBus.registerSubscriber(this);
        assertTrue(eventBus.isRegisteredSubscriber(this));
        eventBus.unregisterSubscriber(this);
        assertFalse(eventBus.isRegisteredSubscriber(this));
    }

    @Test
    public void testIsRegisteredHandler() {
        assertFalse(eventBus.isRegisteredHandler(this));
        eventBus.registerHandler(this);
        assertTrue(eventBus.isRegisteredHandler(this));
        eventBus.unregisterHandler(this);
        assertFalse(eventBus.isRegisteredHandler(this));
    }

    @Test
    public void testPostWithTwoSubscriber() {
        EventBusBasicTest test2 = new EventBusBasicTest();
        eventBus.registerSubscriber(this);
        eventBus.registerSubscriber(test2);
        String event = "Hello";
        eventBus.post(event);
        assertEquals(event, lastStringEvent);
        assertEquals(event, test2.lastStringEvent);
    }

    @Test
    public void testThrowWithTwoHandler() {
        EventBusBasicTest test2 = new EventBusBasicTest();
        eventBus.registerHandler(this);
        eventBus.registerHandler(test2);
        String exceptionalEvent = "Hello";
        eventBus.throwException(exceptionalEvent);
        assertEquals(exceptionalEvent, lastStringExceptionalEvent);
        assertEquals(exceptionalEvent, test2.lastStringExceptionalEvent);
    }

    @Test
    public void testPostMultipleTimes() {
        eventBus.registerSubscriber(this);
        MyEvent event = new MyEvent();
        int count = 1000;
        long start = System.currentTimeMillis();
        // Debug.startMethodTracing("testPostMultipleTimes" + count);
        for (int i = 0; i < count; i++) {
            eventBus.post(event);
        }
        // Debug.stopMethodTracing();
        long time = System.currentTimeMillis() - start;
        log("Posted " + count + " events in " + time + "ms");
        assertEquals(count, countMyEvent);
    }

    @Test
    public void testThrowMultipleTimes() {
        eventBus.registerHandler(this);
        MyExceptionalEvent exceptionalEvent = new MyExceptionalEvent();
        int count = 1000;
        long start = System.currentTimeMillis();
        // Debug.startMethodTracing("testPostMultipleTimes" + count);
        for (int i = 0; i < count; i++) {
            eventBus.throwException(exceptionalEvent);
        }
        // Debug.stopMethodTracing();
        long time = System.currentTimeMillis() - start;
        log("Throwed " + count + " exceptional events in " + time + "ms");
        assertEquals(count, countMyExceptionalEvent);
    }

    @Test
    public void testMultipleSubscribeMethodsForEvent() {
        eventBus.registerSubscriber(this);
        MyEvent event = new MyEvent();
        eventBus.post(event);
        assertEquals(1, countMyEvent);
        assertEquals(1, countMyEvent2);
    }

    @Test
    public void testMultipleHandleMethodsForExceptionalEvent() {
        eventBus.registerHandler(this);
        MyExceptionalEvent exceptionalEvent = new MyExceptionalEvent();
        eventBus.throwException(exceptionalEvent);
        assertEquals(1, countMyExceptionalEvent);
        assertEquals(1, countMyExceptionalEvent2);
    }

    @Test
    public void testPostAfterUnregisterSubscriber() {
        eventBus.registerSubscriber(this);
        eventBus.unregisterSubscriber(this);
        eventBus.post("Hello");
        assertNull(lastStringEvent);
    }

    @Test
    public void testThrowAfterUnregisterHandler() {
        eventBus.registerHandler(this);
        eventBus.unregisterHandler(this);
        eventBus.throwException("Hello");
        assertNull(lastStringExceptionalEvent);
    }

    @Test
    public void testRegisterSubscriberAndPostTwoTypes() {
        eventBus.registerSubscriber(this);
        eventBus.post(42);
        eventBus.post("Hello");
        assertEquals(1, countIntEvent);
        assertEquals(1, countStringEvent);
        assertEquals(42, lastIntEvent);
        assertEquals("Hello", lastStringEvent);
    }

    @Test
    public void testRegisterHandlerAndThrowTwoTypes() {
        eventBus.registerHandler(this);
        eventBus.throwException(42);
        eventBus.throwException("Hello");
        assertEquals(1, countIntExceptionalEvent);
        assertEquals(1, countStringExceptionalEvent);
        assertEquals(42, lastIntExceptionalEvent);
        assertEquals("Hello", lastStringExceptionalEvent);
    }

    @Test
    public void testRegisterUnregisterSubscriberAndPostTwoTypes() {
        eventBus.registerSubscriber(this);
        eventBus.unregisterSubscriber(this);
        eventBus.post(42);
        eventBus.post("Hello");
        assertEquals(0, countIntEvent);
        assertEquals(0, lastIntEvent);
        assertEquals(0, countStringEvent);
    }

    @Test
    public void testRegisterUnregisterHandlerAndPostTwoTypes() {
        eventBus.registerHandler(this);
        eventBus.unregisterHandler(this);
        eventBus.throwException(42);
        eventBus.throwException("Hello");
        assertEquals(0, countIntExceptionalEvent);
        assertEquals(0, lastIntExceptionalEvent);
        assertEquals(0, countStringExceptionalEvent);
    }

    @Test
    public void testPostOnDifferentEventBus() {
        eventBus.registerSubscriber(this);
        new EventBus().post("Hello");
        assertEquals(0, countStringEvent);
    }

    @Test
    public void testThrowOnDifferentEventBus() {
        eventBus.registerHandler(this);
        new EventBus().throwException("Hello");
        assertEquals(0, countStringExceptionalEvent);
    }

    @Test
    public void testPostInEventHandler() {
        RepostInteger reposter = new RepostInteger();
        eventBus.registerSubscriber(reposter);
        eventBus.registerSubscriber(this);
        eventBus.post(1);
        assertEquals(10, countIntEvent);
        assertEquals(10, lastIntEvent);
        assertEquals(10, reposter.countEvent);
        assertEquals(10, reposter.lastEvent);
    }

    @Test
    public void testThrowInExceptionalEventHandler() {
        RethrowInteger rethrow = new RethrowInteger();
        eventBus.registerHandler(rethrow);
        eventBus.registerHandler(this);
        eventBus.throwException(1);
        assertEquals(10, countIntExceptionalEvent);
        assertEquals(10, lastIntExceptionalEvent);
        assertEquals(10, rethrow.countExceptionalEvent);
        assertEquals(10, rethrow.lastExceptionalEvent);
    }

    @Test
    public void testHasSubscriberForEvent() {
        assertFalse(eventBus.hasSubscriberForEventType(String.class));

        eventBus.registerSubscriber(this);
        assertTrue(eventBus.hasSubscriberForEventType(String.class));

        eventBus.unregisterSubscriber(this);
        assertFalse(eventBus.hasSubscriberForEventType(String.class));
    }

    @Test
    public void testHasHandlerForExceptionalEvent() {
        assertFalse(eventBus.hasHandlerForExceptionalEventType(String.class));

        eventBus.registerHandler(this);
        assertTrue(eventBus.hasHandlerForExceptionalEventType(String.class));

        eventBus.unregisterHandler(this);
        assertFalse(eventBus.hasHandlerForExceptionalEventType(String.class));
    }

    @Test
    public void testHasSubscriberForEventSuperclass() {
        assertFalse(eventBus.hasSubscriberForEventType(String.class));

        Object subscriber = new ObjectSubscriber();
        eventBus.registerSubscriber(subscriber);
        assertTrue(eventBus.hasSubscriberForEventType(String.class));

        eventBus.unregisterSubscriber(subscriber);
        assertFalse(eventBus.hasSubscriberForEventType(String.class));
    }

    @Test
    public void testHasHandlerForExceptionalEventSuperclass() {
        assertFalse(eventBus.hasHandlerForExceptionalEventType(String.class));

        Object handler = new ObjectHandler();
        eventBus.registerHandler(handler);
        assertTrue(eventBus.hasHandlerForExceptionalEventType(String.class));

        eventBus.unregisterHandler(handler);
        assertFalse(eventBus.hasHandlerForExceptionalEventType(String.class));
    }

    @Test
    public void testHasSubscriberForEventImplementedInterface() {
        assertFalse(eventBus.hasSubscriberForEventType(String.class));

        Object subscriber = new CharSequenceSubscriber();
        eventBus.registerSubscriber(subscriber);
        assertTrue(eventBus.hasSubscriberForEventType(CharSequence.class));
        assertTrue(eventBus.hasSubscriberForEventType(String.class));

        eventBus.unregisterSubscriber(subscriber);
        assertFalse(eventBus.hasSubscriberForEventType(CharSequence.class));
        assertFalse(eventBus.hasSubscriberForEventType(String.class));
    }

    @Test
    public void testHasHandlerForExceptionalEventImplementedInterface() {
        assertFalse(eventBus.hasHandlerForExceptionalEventType(String.class));

        Object handler = new CharSequenceHandler();
        eventBus.registerHandler(handler);
        assertTrue(eventBus.hasHandlerForExceptionalEventType(CharSequence.class));
        assertTrue(eventBus.hasHandlerForExceptionalEventType(String.class));

        eventBus.unregisterHandler(handler);
        assertFalse(eventBus.hasHandlerForExceptionalEventType(CharSequence.class));
        assertFalse(eventBus.hasHandlerForExceptionalEventType(String.class));
    }

    /** Common flow */

    @Subscribe
    public void onEvent(String event) {
        lastStringEvent = event;
        countStringEvent++;
    }

    @Subscribe
    public void onEvent(Integer event) {
        lastIntEvent = event;
        countIntEvent++;
    }

    @Subscribe
    public void onEvent(MyEvent event) {
        countMyEvent++;
    }

    @Subscribe
    public void onEvent2(MyEvent event) {
        countMyEvent2++;
    }

    @Subscribe
    public void onEvent(MyEventExtended event) {
        countMyEventExtended++;
    }

    public static class StringEventSubscriber {
        public String lastStringEvent;

        @Subscribe
        public void onEvent(String event) {
            lastStringEvent = event;
        }
    }

    public static class CharSequenceSubscriber {
        @Subscribe
        public void onEvent(CharSequence event) {
        }
    }

    public static class ObjectSubscriber {
        @Subscribe
        public void onEvent(Object event) {
        }
    }

    public class MyEvent {
    }

    public class MyEventExtended extends MyEvent {
    }

    public class RepostInteger {
        public int lastEvent;
        public int countEvent;

        @Subscribe
        public void onEvent(Integer event) {
            lastEvent = event;
            countEvent++;
            assertEquals(countEvent, event.intValue());

            if (event < 10) {
                int countIntEventBefore = countEvent;
                eventBus.post(event + 1);
                // All our post calls will just enqueue the event, so check count is unchanged
                assertEquals(countIntEventBefore, countIntEventBefore);
            }
        }
    }

    /** Exceptional flow */

    @Handle
    public void onExceptionalEvent(String exceptionalEvent) {
        lastStringExceptionalEvent = exceptionalEvent;
        countStringExceptionalEvent++;
    }

    @Handle
    public void onExceptionalEvent(Integer exceptionalEvent) {
        lastIntExceptionalEvent = exceptionalEvent;
        countIntExceptionalEvent++;
    }

    @Handle
    public void onExceptionalEvent(MyExceptionalEvent exceptionalEvent) {
        countMyExceptionalEvent++;
    }

    @Handle
    public void onExceptionalEvent2(MyExceptionalEvent exceptionalEvent) {
        countMyExceptionalEvent2++;
    }

    @Handle
    public void onExceptionalEvent(MyExceptionalEventExtended exceptionalEvent) {
        countMyExceptionalEventExtended++;
    }

    public static class StringExceptionalEventHandler {
        public String lastStringExceptionalEvent;

        @Handle
        public void onExceptionalEvent(String exceptionalEvent) {
            lastStringExceptionalEvent = exceptionalEvent;
        }
    }

    public static class CharSequenceHandler {
        @Handle
        public void onExceptionalEvent(CharSequence exceptionalEvent) {
        }
    }

    public static class ObjectHandler {
        @Handle
        public void onExceptionalEvent(Object exceptionalEvent) {
        }
    }

    public class MyExceptionalEvent {
    }

    public class MyExceptionalEventExtended extends MyExceptionalEvent {
    }

    public class RethrowInteger {
        public int lastExceptionalEvent;
        public int countExceptionalEvent;

        @Handle
        public void onExceptionalEvent(Integer exceptionalEvent) {
            lastExceptionalEvent = exceptionalEvent;
            countExceptionalEvent++;
            assertEquals(countExceptionalEvent, exceptionalEvent.intValue());

            if (exceptionalEvent < 10) {
                int countIntExceptionalEventBefore = countExceptionalEvent;
                eventBus.throwException(exceptionalEvent + 1);
                // All our post calls will just enqueue the event, so check count is unchanged
                assertEquals(countIntExceptionalEventBefore, countIntExceptionalEventBefore);
            }
        }
    }
}
