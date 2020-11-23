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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Fabiano Gadelha
 */
public class EventBusStickyExceptionalEventTest extends AbstractEventBusTest {

    @Test
    public void testThrowSticky() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        eventBus.registerHandler(this);
        assertEquals("Sticky", lastExceptionalEvent);
        assertEquals(Thread.currentThread(), lastExceptionalThread);
    }

    @Test
    public void testThrowStickyTwoExceptionalEvents() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        eventBus.throwSticky(new IntTest(7));
        eventBus.registerHandler(this);
        assertEquals(2, exceptionalEventCount.intValue());
    }

    @Test
    public void testThrowStickyTwoHandlers() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        eventBus.throwSticky(new IntTest(7));
        eventBus.registerHandler(this);
        StickyIntTestHandler handler2 = new StickyIntTestHandler();
        eventBus.registerHandler(handler2);
        assertEquals(3, exceptionalEventCount.intValue());

        eventBus.throwSticky("Sticky");
        assertEquals(4, exceptionalEventCount.intValue());

        eventBus.throwSticky(new IntTest(8));
        assertEquals(6, exceptionalEventCount.intValue());
    }

    @Test
    public void testThrowStickyRegisterNonSticky() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        eventBus.registerHandler(new NonStickyHandler());
        assertNull(lastExceptionalEvent);
        assertEquals(0, exceptionalEventCount.intValue());
    }

    @Test
    public void testThrowNonStickyRegisterSticky() throws InterruptedException {
        eventBus.throwException("NonSticky");
        eventBus.registerHandler(this);
        assertNull(lastExceptionalEvent);
        assertEquals(0, exceptionalEventCount.intValue());
    }

    @Test
    public void testThrowStickyTwice() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        eventBus.throwSticky("NewSticky");
        eventBus.registerHandler(this);
        assertEquals("NewSticky", lastExceptionalEvent);
    }

    @Test
    public void testThrowStickyThenThrowNormal() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        eventBus.throwException("NonSticky");
        eventBus.registerHandler(this);
        assertEquals("Sticky", lastExceptionalEvent);
    }

    @Test
    public void testThrowStickyWithRegisterAndUnregisterHandler() throws InterruptedException {
        eventBus.registerHandler(this);
        eventBus.throwSticky("Sticky");
        assertEquals("Sticky", lastExceptionalEvent);

        eventBus.unregisterHandler(this);
        eventBus.registerHandler(this);
        assertEquals("Sticky", lastExceptionalEvent);
        assertEquals(2, exceptionalEventCount.intValue());

        eventBus.throwSticky("NewSticky");
        assertEquals(3, exceptionalEventCount.intValue());
        assertEquals("NewSticky", lastExceptionalEvent);

        eventBus.unregisterHandler(this);
        eventBus.registerHandler(this);
        assertEquals(4, exceptionalEventCount.intValue());
        assertEquals("NewSticky", lastExceptionalEvent);
    }

    @Test
    public void testThrowStickyAndGet() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        assertEquals("Sticky", eventBus.getStickyExceptionalEvent(String.class));
    }

    @Test
    public void testThrowStickyRemoveClass() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        eventBus.removeStickyExceptionalEvent(String.class);
        assertNull(eventBus.getStickyExceptionalEvent(String.class));
        eventBus.registerHandler(this);
        assertNull(lastExceptionalEvent);
        assertEquals(0, exceptionalEventCount.intValue());
    }

    @Test
    public void testThrowStickyRemoveExceptionalEvent() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        assertTrue(eventBus.removeStickyExceptionalEvent("Sticky"));
        assertNull(eventBus.getStickyExceptionalEvent(String.class));
        eventBus.registerHandler(this);
        assertNull(lastExceptionalEvent);
        assertEquals(0, exceptionalEventCount.intValue());
    }

    @Test
    public void testThrowStickyRemoveAll() throws InterruptedException {
        eventBus.throwSticky("Sticky");
        eventBus.throwSticky(new IntTest(77));
        eventBus.removeAllStickyExceptionalEvents();
        assertNull(eventBus.getStickyExceptionalEvent(String.class));
        assertNull(eventBus.getStickyExceptionalEvent(IntTest.class));
        eventBus.registerHandler(this);
        assertNull(lastExceptionalEvent);
        assertEquals(0, exceptionalEventCount.intValue());
    }

    @Test
    public void testRemoveStickyExceptionalEventInHandler() throws InterruptedException {
        eventBus.registerHandler(new RemoveStickyHandler());
        eventBus.throwSticky("Sticky");
        eventBus.registerHandler(this);
        assertNull(lastExceptionalEvent);
        assertEquals(0, exceptionalEventCount.intValue());
        assertNull(eventBus.getStickyExceptionalEvent(String.class));
    }

    @Handle(sticky = true)
    public void onExceptionalEvent(String exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
    }

    @Handle(sticky = true)
    public void onExceptionalEvent(IntTest exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
    }

    public class RemoveStickyHandler {
        @SuppressWarnings("unused")
        @Handle(sticky = true)
        public void onExceptionalEvent(String exceptionalEvent) {
            eventBus.removeStickyExceptionalEvent(exceptionalEvent);
        }
    }

    public class NonStickyHandler {
        @Handle
        public void onExceptionalEvent(String exceptionalEvent) {
            trackExceptionalEvent(exceptionalEvent);
        }

        @Handle
        public void onExceptionalEvent(IntTest exceptionalEvent) {
            trackExceptionalEvent(exceptionalEvent);
        }
    }

    public class StickyIntTestHandler {
        @Handle(sticky = true)
        public void onExceptionalEvent(IntTest exceptionalEvent) {
            trackExceptionalEvent(exceptionalEvent);
        }
    }
}
