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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusBuilderTest extends AbstractEventBusTest {

    @Test
    public void testThrowSubscriberException() {
        eventBus = EventBus.builder().throwSubscriberException(true).build();
        eventBus.registerSubscriber(new SubscriberExceptionEventTracker());
        eventBus.registerSubscriber(new ThrowingSubscriber());
        try {
            eventBus.post("Foo");
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    @Test
    public void testThrowHandlerException() {
        eventBus = EventBus.builder().throwHandlerException(true).build();
        eventBus.registerHandler(new HandlerExceptionExceptionalEventTracker());
        eventBus.registerHandler(new ThrowingHandler());
        try {
            eventBus.throwException("Foo");
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    @Test
    public void testDoNotSendSubscriberExceptionEvent() {
        eventBus = EventBus.builder().logSubscriberExceptions(false).sendSubscriberExceptionEvent(false).build();
        eventBus.registerSubscriber(new SubscriberExceptionEventTracker());
        eventBus.registerSubscriber(new ThrowingSubscriber());
        eventBus.post("Foo");
        assertEventCount(0);
    }

    @Test
    public void testDoNotSendHandlerExceptionExceptionalEvent() {
        eventBus = EventBus.builder().logHandlerExceptions(false).sendHandlerExceptionExceptionalEvent(false).build();
        eventBus.registerHandler(new HandlerExceptionExceptionalEventTracker());
        eventBus.registerHandler(new ThrowingHandler());
        eventBus.throwException("Foo");
        assertExceptionalEventCount(0);
    }

    @Test
    public void testDoNotSendNoSubscriberEvent() {
        eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).build();
        eventBus.registerSubscriber(new NoSubscriberEventTracker());
        eventBus.post("Foo");
        assertEventCount(0);
    }

    @Test
    public void testDoNotSendNoHandlerExceptionalEvent() {
        eventBus = EventBus.builder().logNoHandlerMessages(false).sendNoHandlerExceptionalEvent(false).build();
        eventBus.registerHandler(new NoHandlerExceptionalEventTracker());
        eventBus.throwException("Foo");
        assertExceptionalEventCount(0);
    }

    @Test
    public void testInstallDefaultEventBus() {
        EventBusBuilder builder = EventBus.builder();
        try {
            // Either this should throw when another unit test got the default event bus...
            eventBus = builder.installDefaultEventBus();
            Assert.assertEquals(eventBus, EventBus.getDefault());

            // ...or this should throw
            eventBus = builder.installDefaultEventBus();
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    @Test
    public void testEventInheritance() {
        eventBus = EventBus.builder()
                .eventInheritance(false)
                .exceptionalEventInheritance(false)
                .build();
        eventBus.registerSubscriber(new ThrowingSubscriber());
        eventBus.post("Foo");
    }

    @Test
    public void testExceptionalEventInheritance() {
        eventBus = EventBus.builder()
                .exceptionalEventInheritance(false)
                .eventInheritance(false)
                .build();
        eventBus.registerHandler(new ThrowingHandler());
        eventBus.throwException("Foo");
    }

    /** Common flow */

    public class SubscriberExceptionEventTracker {
        @Subscribe
        public void onEvent(SubscriberExceptionEvent event) {
            trackEvent(event);
        }
    }

    public class NoSubscriberEventTracker {
        @Subscribe
        public void onEvent(NoSubscriberEvent event) {
            trackEvent(event);
        }
    }

    public class ThrowingSubscriber {
        @Subscribe
        public void onEvent(Object event) {
            throw new RuntimeException();
        }
    }

    /** Exceptional flow */

    public class HandlerExceptionExceptionalEventTracker {
        @Handle
        public void onExceptionalEvent(HandlerExceptionExceptionalEvent exceptionalEvent) {
            trackExceptionalEvent(exceptionalEvent);
        }
    }

    public class NoHandlerExceptionalEventTracker {
        @Handle
        public void onExceptionalEvent(NoHandlerExceptionalEvent exceptionalEvent) {
            trackExceptionalEvent(exceptionalEvent);
        }
    }

    public class ThrowingHandler {
        @Handle
        public void onExceptionalEvent(Object exceptionalEvent) {
            throw new RuntimeException();
        }
    }
}
