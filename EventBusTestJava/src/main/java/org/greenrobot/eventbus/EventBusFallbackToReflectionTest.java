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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventBusFallbackToReflectionTest extends AbstractEventBusTest {
    @Test
    public void testAnonymousSubscriberClass() {
        Object subscriber = new Object() {
            @Subscribe
            public void onEvent(String event) {
                trackEvent(event);
            }
        };
        eventBus.registerSubscriber(subscriber);

        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(1, eventsReceived.size());
    }

    @Test
    public void testAnonymousHandlerClass() {
        Object handler = new Object() {
            @Handle
            public void onExceptionalEvent(String exceptionalEvent) {
                trackExceptionalEvent(exceptionalEvent);
            }
        };
        eventBus.registerHandler(handler);

        eventBus.throwException("Hello");
        assertEquals("Hello", lastExceptionalEvent);
        assertEquals(1, exceptionalEventsReceived.size());
    }

    @Test
    public void testAnonymousSubscriberClassWithPublicSuperclass() {
        Object subscriber = new PublicClass() {
            @Subscribe
            public void onEvent(String event) {
                trackEvent(event);
            }
        };
        eventBus.registerSubscriber(subscriber);

        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(2, eventsReceived.size());
    }

    @Test
    public void testAnonymousHandlerClassWithPublicSuperclass() {
        Object handler = new PublicClassExceptional() {
            @Handle
            public void onExceptionalEvent(String exceptionalEvent) {
                trackExceptionalEvent(exceptionalEvent);
            }
        };
        eventBus.registerHandler(handler);

        eventBus.throwException("Hello");
        assertEquals("Hello", lastExceptionalEvent);
        assertEquals(2, exceptionalEventsReceived.size());
    }

    @Test
    public void testAnonymousSubscriberClassWithPrivateSuperclass() {
        eventBus.registerSubscriber(new PublicWithPrivateSuperClass());
        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(2, eventsReceived.size());
    }

    @Test
    public void testAnonymousHandlerClassWithPrivateSuperclass() {
        eventBus.registerHandler(new PublicWithPrivateSuperClassExceptional());
        eventBus.throwException("Hello");
        assertEquals("Hello", lastExceptionalEvent);
        assertEquals(2, exceptionalEventsReceived.size());
    }

    @Test
    public void testSubscriberClassWithPrivateEvent() {
        eventBus.registerSubscriber(new PublicClassWithPrivateEvent());
        PrivateEvent privateEvent = new PrivateEvent();
        eventBus.post(privateEvent);
        assertEquals(privateEvent, lastEvent);
        assertEquals(1, eventsReceived.size());
    }

    @Test
    public void testHandlerClassWithPrivateExceptionalEvent() {
        eventBus.registerHandler(new PublicClassExceptionalWithPrivateExceptionalEvent());
        PrivateExceptionalEvent privateExceptionalEvent = new PrivateExceptionalEvent();
        eventBus.throwException(privateExceptionalEvent);
        assertEquals(privateExceptionalEvent, lastExceptionalEvent);
        assertEquals(1, exceptionalEventsReceived.size());
    }

    @Test
    public void testSubscriberClassWithPublicAndPrivateEvent() {
        eventBus.registerSubscriber(new PublicClassWithPublicAndPrivateEvent());

        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(1, eventsReceived.size());

        PrivateEvent privateEvent = new PrivateEvent();
        eventBus.post(privateEvent);
        assertEquals(privateEvent, lastEvent);
        assertEquals(2, eventsReceived.size());
    }

    @Test
    public void testHandlerClassWithPublicAndPrivateExceptionalEvent() {
        eventBus.registerHandler(new PublicClassExceptionalWithPublicAndPrivateExceptionalEvent());

        eventBus.throwException("Hello");
        assertEquals("Hello", lastExceptionalEvent);
        assertEquals(1, exceptionalEventsReceived.size());

        PrivateExceptionalEvent privateExceptionalEvent = new PrivateExceptionalEvent();
        eventBus.throwException(privateExceptionalEvent);
        assertEquals(privateExceptionalEvent, lastExceptionalEvent);
        assertEquals(2, exceptionalEventsReceived.size());
    }

    @Test
    public void testSubscriberExtendingClassWithPrivateEvent() {
        eventBus.registerSubscriber(new PublicWithPrivateEventInSuperclass());
        PrivateEvent privateEvent = new PrivateEvent();
        eventBus.post(privateEvent);
        assertEquals(privateEvent, lastEvent);
        assertEquals(2, eventsReceived.size());
    }

    @Test
    public void testHandlerExtendingClassWithPrivateExceptionalEvent() {
        eventBus.registerHandler(new PublicWithPrivateExceptionalEventInSuperclassExceptional());
        PrivateExceptionalEvent privateExceptionalEvent = new PrivateExceptionalEvent();
        eventBus.throwException(privateExceptionalEvent);
        assertEquals(privateExceptionalEvent, lastExceptionalEvent);
        assertEquals(2, exceptionalEventsReceived.size());
    }

    public EventBusFallbackToReflectionTest() {
        super(true, true);
    }

    /** Common flow */

    private class PrivateEvent {
    }

    public class PublicClass {
        @Subscribe
        public void onEvent(Object any) {
            trackEvent(any);
        }
    }

    private class PrivateClass {
        @Subscribe
        public void onEvent(Object any) {
            trackEvent(any);
        }
    }

    public class PublicWithPrivateSuperClass extends PrivateClass {
        @Subscribe
        public void onEvent(String any) {
            trackEvent(any);
        }
    }

    public class PublicClassWithPrivateEvent {
        @Subscribe
        public void onEvent(PrivateEvent any) {
            trackEvent(any);
        }
    }

    public class PublicClassWithPublicAndPrivateEvent {
        @Subscribe
        public void onEvent(String any) {
            trackEvent(any);
        }

        @Subscribe
        public void onEvent(PrivateEvent any) {
            trackEvent(any);
        }
    }

    public class PublicWithPrivateEventInSuperclass extends PublicClassWithPrivateEvent {
        @Subscribe
        public void onEvent(Object any) {
            trackEvent(any);
        }
    }

    /** Exceptional flow */

    private class PrivateExceptionalEvent {
    }

    public class PublicClassExceptional {
        @Handle
        public void onExceptionalEvent(Object any) {
            trackExceptionalEvent(any);
        }
    }

    private class PrivateClassExceptional {
        @Handle
        public void onExceptionalEvent(Object any) {
            trackExceptionalEvent(any);
        }
    }

    public class PublicWithPrivateSuperClassExceptional extends PrivateClassExceptional {
        @Handle
        public void onExceptionalEvent(String any) {
            trackExceptionalEvent(any);
        }
    }

    public class PublicClassExceptionalWithPrivateExceptionalEvent {
        @Handle
        public void onExceptionalEvent(PrivateExceptionalEvent any) {
            trackExceptionalEvent(any);
        }
    }

    public class PublicClassExceptionalWithPublicAndPrivateExceptionalEvent {
        @Handle
        public void onExceptionalEvent(String any) {
            trackExceptionalEvent(any);
        }

        @Handle
        public void onExceptionalEvent(PrivateExceptionalEvent any) {
            trackExceptionalEvent(any);
        }
    }

    public class PublicWithPrivateExceptionalEventInSuperclassExceptional extends PublicClassExceptionalWithPrivateExceptionalEvent {
        @Handle
        public void onExceptionalEvent(Object any) {
            trackExceptionalEvent(any);
        }
    }
}
