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

public class EventBusGenericsTest extends AbstractEventBusTest {
    @Test
    public void testGenericEventAndSubscriber() {
        GenericEventSubscriber<IntTest> genericSubscriber = new GenericEventSubscriber<IntTest>();
        eventBus.registerSubscriber(genericSubscriber);
        eventBus.post(new GenericEvent<Integer>());
        assertEventCount(1);
    }

    @Test
    public void testGenericExceptionalEventAndHandler() {
        GenericExceptionalEventHandler<IntTest> genericHandler = new GenericExceptionalEventHandler<IntTest>();
        eventBus.registerHandler(genericHandler);
        eventBus.throwException(new GenericExceptionalEvent<Integer>());
        assertExceptionalEventCount(1);
    }

    @Test
    public void testGenericEventAndSubscriber_TypeErasure() {
        FullGenericEventSubscriber<IntTest> genericSubscriber = new FullGenericEventSubscriber<IntTest>();
        eventBus.registerSubscriber(genericSubscriber);
        eventBus.post(new IntTest(42));
        eventBus.post("Type erasure!");
        assertEventCount(2);
    }

    @Test
    public void testGenericExceptionalEventAndHandler_TypeErasure() {
        FullGenericExceptionalEventHandler<IntTest> genericHandler = new FullGenericExceptionalEventHandler<IntTest>();
        eventBus.registerHandler(genericHandler);
        eventBus.throwException(new IntTest(42));
        eventBus.throwException("Type erasure!");
        assertExceptionalEventCount(2);
    }

    @Test
    public void testGenericEventAndSubscriber_BaseType() {
        GenericNumberEventSubscriber<Float> genericSubscriber = new GenericNumberEventSubscriber<>();
        eventBus.registerSubscriber(genericSubscriber);
        eventBus.post(new Float(42));
        eventBus.post(new Double(23));
        assertEventCount(2);
        eventBus.post("Not the same base type");
        assertEventCount(2);
    }

    @Test
    public void testGenericExceptionalEventAndHandler_BaseType() {
        GenericNumberExceptionalEventHandler<Float> genericHandler = new GenericNumberExceptionalEventHandler<>();
        eventBus.registerHandler(genericHandler);
        eventBus.throwException(new Float(42));
        eventBus.throwException(new Double(23));
        assertExceptionalEventCount(2);
        eventBus.throwException("Not the same base type");
        assertExceptionalEventCount(2);
    }

    @Test
    public void testGenericEventAndSubscriber_Subclass() {
        GenericFloatEventSubscriber genericSubscriber = new GenericFloatEventSubscriber();
        eventBus.registerSubscriber(genericSubscriber);
        eventBus.post(new Float(42));
        eventBus.post(new Double(77));
        assertEventCount(2);
        eventBus.post("Not the same base type");
        assertEventCount(2);
    }

    @Test
    public void testGenericExceptionalEventAndHandler_Subclass() {
        GenericFloatExceptionalEventHandler genericHandler = new GenericFloatExceptionalEventHandler();
        eventBus.registerHandler(genericHandler);
        eventBus.throwException(new Float(42));
        eventBus.throwException(new Double(77));
        assertExceptionalEventCount(2);
        eventBus.throwException("Not the same base type");
        assertExceptionalEventCount(2);
    }

    /** Common flow */

    public static class GenericEvent<T> {
        T value;
    }

    public class GenericEventSubscriber<T> {
        @Subscribe
        public void onGenericEvent(GenericEvent<T> event) {
            trackEvent(event);
        }
    }

    public class FullGenericEventSubscriber<T> {
        @Subscribe
        public void onGenericEvent(T event) {
            trackEvent(event);
        }
    }

    public class GenericNumberEventSubscriber<T extends Number> {
        @Subscribe
        public void onGenericEvent(T event) {
            trackEvent(event);
        }
    }

    public class GenericFloatEventSubscriber extends GenericNumberEventSubscriber<Float> {
    }

    /** Exceptional flow */

    public static class GenericExceptionalEvent<T> {
        T value;
    }

    public class GenericExceptionalEventHandler<T> {
        @Handle
        public void onGenericExceptionalEvent(GenericExceptionalEvent<T> exceptionalEvent) {
            trackExceptionalEvent(exceptionalEvent);
        }
    }

    public class FullGenericExceptionalEventHandler<T> {
        @Handle
        public void onGenericExceptionalEvent(T exceptionalEvent) {
            trackExceptionalEvent(exceptionalEvent);
        }
    }

    public class GenericNumberExceptionalEventHandler<T extends Number> {
        @Handle
        public void onGenericExceptionalEvent(T exceptionalEvent) {
            trackExceptionalEvent(exceptionalEvent);
        }
    }

    public class GenericFloatExceptionalEventHandler extends GenericNumberExceptionalEventHandler<Float> {
    }
}
