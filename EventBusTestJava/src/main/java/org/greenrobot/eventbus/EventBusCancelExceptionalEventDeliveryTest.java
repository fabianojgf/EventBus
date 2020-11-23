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

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class EventBusCancelExceptionalEventDeliveryTest extends AbstractEventBusTest {

    Throwable failed;

    @Test
    public void testCancelThrow() {
        Handler canceler = new Handler(1, true);
        eventBus.registerHandler(new Handler(0, false));
        eventBus.registerHandler(canceler);
        eventBus.registerHandler(new Handler(0, false));
        eventBus.throwException("42");
        assertEquals(1, exceptionalEventCount.intValue());

        eventBus.unregisterHandler(canceler);
        eventBus.throwException("42");
        assertEquals(1 + 2, exceptionalEventCount.intValue());
    }

    @Test
    public void testCancelThrowInBetween() {
        eventBus.registerHandler(new Handler(2, true));
        eventBus.registerHandler(new Handler(1, false));
        eventBus.registerHandler(new Handler(3, false));
        eventBus.throwException("42");
        assertEquals(2, exceptionalEventCount.intValue());
    }

    @Test
    public void testCancelThrowOutsideExceptionalEventHandler() {
        try {
            eventBus.cancelExceptionalEventDelivery(this);
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    @Test
    public void testCancelWrongExceptionalEvent() {
        eventBus.registerHandler(new HandlerCancelOtherExceptionalEvent());
        eventBus.throwException("42");
        assertEquals(0, exceptionalEventCount.intValue());
        assertNotNull(failed);
    }

    public class Handler {
        private final int prio;
        private final boolean cancel;

        public Handler(int prio, boolean cancel) {
            this.prio = prio;
            this.cancel = cancel;
        }

        @Handle
        public void onExceptionalEvent(String exceptionalEvent) {
            handleExceptionalEvent(exceptionalEvent, 0);
        }

        @Handle(priority = 1)
        public void onExceptionalEvent1(String exceptionalEvent) {
            handleExceptionalEvent(exceptionalEvent, 1);
        }

        @Handle(priority = 2)
        public void onExceptionalEvent2(String exceptionalEvent) {
            handleExceptionalEvent(exceptionalEvent, 2);
        }

        @Handle(priority = 3)
        public void onExceptionalEvent3(String exceptionalEvent) {
            handleExceptionalEvent(exceptionalEvent, 3);
        }

        private void handleExceptionalEvent(String exceptionalEvent, int prio) {
            if(this.prio == prio) {
                trackExceptionalEvent(exceptionalEvent);
                if (cancel) {
                    eventBus.cancelExceptionalEventDelivery(exceptionalEvent);
                }
            }
        }
    }

    public class HandlerCancelOtherExceptionalEvent {
        @Handle
        public void onExceptionalEvent(String exceptionalEvent) {
            try {
                eventBus.cancelExceptionalEventDelivery(this);
            } catch (EventBusException e) {
                failed = e;
            }
        }
    }

    public class HandlerMainThread {
        final CountDownLatch done = new CountDownLatch(1);

        @Handle(threadMode = ExceptionalThreadMode.MAIN)
        public void onExceptionalEventMainThread(String exceptionalEvent) {
            try {
                eventBus.cancelExceptionalEventDelivery(exceptionalEvent);
            } catch (EventBusException e) {
                failed = e;
            }
            done.countDown();
        }
    }
}
