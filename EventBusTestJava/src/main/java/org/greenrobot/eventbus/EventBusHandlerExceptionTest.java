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
 * @author Markus Junginger, greenrobot
 */
public class EventBusHandlerExceptionTest extends AbstractEventBusTest {

    @Test
    public void testHandlerExceptionExceptionalEvent() {
        eventBus = EventBus.builder().logHandlerExceptions(false).build();
        eventBus.registerHandler(this);
        eventBus.throwException("Foo");
        assertExceptionalEventCount(1);
        assertEquals(HandlerExceptionExceptionalEvent.class, lastExceptionalEvent.getClass());
        HandlerExceptionExceptionalEvent exExceptionalEvent = (HandlerExceptionExceptionalEvent) lastExceptionalEvent;
        assertEquals("Foo", exExceptionalEvent.causingExceptionalEvent);
        assertSame(this, exExceptionalEvent.causingHandler);
        assertEquals("Bar", exExceptionalEvent.throwable.getMessage());
    }

    @Test
    public void testBadExceptionHandler() {
        eventBus = EventBus.builder().logHandlerExceptions(false).build();
        eventBus.registerHandler(this);
        eventBus.registerHandler(new BadExceptionHandler());
        eventBus.throwException("Foo");
        assertExceptionalEventCount(1);
    }

    @Handle
    public void onExceptionalEvent(String event) {
        throw new RuntimeException("Bar");
    }

    @Handle
    public void onExceptionalEvent(HandlerExceptionExceptionalEvent event) {
        trackExceptionalEvent(event);
    }

    public class BadExceptionHandler {
        @Handle
        public void onExceptionalEvent(HandlerExceptionExceptionalEvent event) {
            throw new RuntimeException("Bad");
        }
    }

}
