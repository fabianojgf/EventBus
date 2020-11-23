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
import static org.junit.Assert.assertSame;

/**
 * @author Fabiano Gadelha
 */
public class EventBusNoHandlerExceptionalEventTest extends AbstractEventBusTest {

    @Test
    public void testNoHandlerExceptionalEvent() {
        eventBus.registerHandler(this);
        eventBus.throwException("Foo");
        assertExceptionalEventCount(1);
        assertEquals(NoHandlerExceptionalEvent.class, lastExceptionalEvent.getClass());
        NoHandlerExceptionalEvent noHandler = (NoHandlerExceptionalEvent) lastExceptionalEvent;
        assertEquals("Foo", noHandler.originalExceptionalEvent);
        assertSame(eventBus, noHandler.eventBus);
    }

    @Test
    public void testNoHandlerEventAfterUnregisterHandler() {
        Object handler = new DummyHandler();
        eventBus.registerHandler(handler);
        eventBus.unregisterHandler(handler);
        testNoHandlerExceptionalEvent();
    }

    @Test
    public void testBadNoHandlerHandler() {
        eventBus = EventBus.builder().logNoHandlerMessages(false).build();
        eventBus.registerHandler(this);
        eventBus.registerHandler(new BadNoHandlerHandler());
        eventBus.throwException("Foo");
        assertExceptionalEventCount(2);

        assertEquals(HandlerExceptionExceptionalEvent.class, lastExceptionalEvent.getClass());
        NoHandlerExceptionalEvent noHandler = (NoHandlerExceptionalEvent) ((HandlerExceptionExceptionalEvent) lastExceptionalEvent).causingExceptionalEvent;
        assertEquals("Foo", noHandler.originalExceptionalEvent);
    }

    @Handle
    public void onExceptionalEvent(NoHandlerExceptionalEvent exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
    }

    @Handle
    public void onExceptionalEvent(HandlerExceptionExceptionalEvent exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
    }

    public static class DummyHandler {
        @SuppressWarnings("unused")
        @Handle
        public void onExceptionalEvent(String dummy) {
        }
    }

    public class BadNoHandlerHandler {
        @Handle
        public void onExceptionalEvent(NoHandlerExceptionalEvent exceptionalEvent) {
            throw new RuntimeException("I'm bad");
        }
    }
}
