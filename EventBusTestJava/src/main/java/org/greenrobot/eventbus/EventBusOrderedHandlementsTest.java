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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabiano Gadelha
 */
public class EventBusOrderedHandlementsTest extends AbstractEventBusTest {

    int lastPrio = Integer.MAX_VALUE;
    final List<PrioHandler> registered = new ArrayList<PrioHandler>();
    private String fail;

    @Test
    public void testOrdered() {
        runTestOrdered("42", false, 5);
    }

    @Test
    public void testOrderedMainThread() {
        runTestOrdered(new IntTest(42), false, 3);
    }

    @Test
    public void testOrderedBackgroundThread() {
        runTestOrdered(Integer.valueOf(42), false, 3);
    }

    @Test
    public void testOrderedSticky() {
        runTestOrdered("42", true, 5);
    }

    @Test
    public void testOrderedMainThreadSticky() {
        runTestOrdered(new IntTest(42), true, 3);
    }

    @Test
    public void testOrderedBackgroundThreadSticky() {
        runTestOrdered(Integer.valueOf(42), true, 3);
    }

    protected void runTestOrdered(Object event, boolean sticky, int expectedExceptionalEventCount) {
        Object handler = sticky ? new PrioHandlerSticky() : new PrioHandler();
        eventBus.registerHandler(handler);
        eventBus.throwException(event);

        waitForExceptionalEventCount(expectedExceptionalEventCount, 10000);
        assertEquals(null, fail);

        eventBus.unregisterHandler(handler);
    }

    public final class PrioHandler {
        @Handle(priority = 1)
        public void onExceptionalEventP1(String exceptionalEvent) {
            handleExceptionalEvent(1, exceptionalEvent);
        }

        @Handle(priority = -1)
        public void onExceptionalEventM1(String exceptionalEvent) {
            handleExceptionalEvent(-1, exceptionalEvent);
        }

        @Handle(priority = 0)
        public void onExceptionalEventP0(String exceptionalEvent) {
            handleExceptionalEvent(0, exceptionalEvent);
        }

        @Handle(priority = 10)
        public void onExceptionalEventP10(String exceptionalEvent) {
            handleExceptionalEvent(10, exceptionalEvent);
        }

        @Handle(priority = -100)
        public void onExceptionalEventM100(String exceptionalEvent) {
            handleExceptionalEvent(-100, exceptionalEvent);
        }


        @Handle(threadMode = ExceptionalThreadMode.MAIN, priority = -1)
        public void onExceptionalEventMainThreadM1(IntTest exceptionalEvent) {
            handleExceptionalEvent(-1, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.MAIN)
        public void onExceptionalEventMainThreadP0(IntTest exceptionalEvent) {
            handleExceptionalEvent(0, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.MAIN, priority = 1)
        public void onExceptionalEventMainThreadP1(IntTest exceptionalEvent) {
            handleExceptionalEvent(1, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.BACKGROUND, priority = 1)
        public void onExceptionalEventBackgroundThreadP1(Integer exceptionalEvent) {
            handleExceptionalEvent(1, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.BACKGROUND)
        public void onExceptionalEventBackgroundThreadP0(Integer exceptionalEvent) {
            handleExceptionalEvent(0, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.BACKGROUND, priority = -1)
        public void onExceptionalEventBackgroundThreadM1(Integer exceptionalEvent) {
            handleExceptionalEvent(-1, exceptionalEvent);
        }

        protected void handleExceptionalEvent(int prio, Object exceptionalEvent) {
            if (prio > lastPrio) {
                fail = "Called prio " + prio + " after " + lastPrio;
            }
            lastPrio = prio;

            log("Handler " + prio + " got: " + exceptionalEvent);
            trackExceptionalEvent(exceptionalEvent);
        }

    }

    public final class PrioHandlerSticky {
        @Handle(priority = 1, sticky = true)
        public void onExceptionalEventP1(String exceptionalEvent) {
            handleExceptionalEvent(1, exceptionalEvent);
        }


        @Handle(priority = -1, sticky = true)
        public void onExceptionalEventM1(String exceptionalEvent) {
            handleExceptionalEvent(-1, exceptionalEvent);
        }

        @Handle(priority = 0, sticky = true)
        public void onExceptionalEventP0(String exceptionalEvent) {
            handleExceptionalEvent(0, exceptionalEvent);
        }

        @Handle(priority = 10, sticky = true)
        public void onExceptionalEventP10(String exceptionalEvent) {
            handleExceptionalEvent(10, exceptionalEvent);
        }

        @Handle(priority = -100, sticky = true)
        public void onExceptionalEventM100(String exceptionalEvent) {
            handleExceptionalEvent(-100, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.MAIN, priority = -1, sticky = true)
        public void onExceptionalEventMainThreadM1(IntTest exceptionalEvent) {
            handleExceptionalEvent(-1, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.MAIN, sticky = true)
        public void onExceptionalEventMainThreadP0(IntTest exceptionalEvent) {
            handleExceptionalEvent(0, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.MAIN, priority = 1, sticky = true)
        public void onExceptionalEventMainThreadP1(IntTest exceptionalEvent) {
            handleExceptionalEvent(1, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.BACKGROUND, priority = 1, sticky = true)
        public void onExceptionalEventBackgroundThreadP1(Integer exceptionalEvent) {
            handleExceptionalEvent(1, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.BACKGROUND, sticky = true)
        public void onExceptionalEventBackgroundThreadP0(Integer exceptionalEvent) {
            handleExceptionalEvent(0, exceptionalEvent);
        }

        @Handle(threadMode = ExceptionalThreadMode.BACKGROUND, priority = -1, sticky = true)
        public void onExceptionalEventBackgroundThreadM1(Integer exceptionalEvent) {
            handleExceptionalEvent(-1, exceptionalEvent);
        }

        protected void handleExceptionalEvent(int prio, Object exceptionalEvent) {
            if (prio > lastPrio) {
                fail = "Called prio " + prio + " after " + lastPrio;
            }
            lastPrio = prio;

            log("Handler " + prio + " got: " + exceptionalEvent);
            trackExceptionalEvent(exceptionalEvent);
        }

    }

}
