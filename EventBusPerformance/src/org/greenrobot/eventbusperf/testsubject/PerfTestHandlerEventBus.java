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

package org.greenrobot.eventbusperf.testsubject;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.ExceptionalThreadMode;
import org.greenrobot.eventbus.Handle;
import org.greenrobot.eventbus.meta.HandlerInfoIndex;
import org.greenrobot.eventbusperf.MyEventBusIndex;
import org.greenrobot.eventbusperf.TestExceptionalEvent;
import org.greenrobot.eventbusperf.TestHandler;
import org.greenrobot.eventbusperf.TestHandlerParams;
import org.greenrobot.eventbusperf.TestEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class PerfTestHandlerEventBus extends TestHandler {

    private final EventBus eventBus;
    private final ArrayList<Object> handlers;
    private final Class<?> handlerClass;
    private final int exceptionalEventCount;
    private final int expectedExceptionalEventCount;

    public PerfTestHandlerEventBus(Context context, TestHandlerParams params) {
        super(context, params);
        eventBus = EventBus.builder().exceptionalEventInheritance(params.isExceptionalEventInheritance()).addIndex((HandlerInfoIndex) new MyEventBusIndex())
                .ignoreGeneratedIndex(params.isIgnoreGeneratedIndex()).build();
        handlers = new ArrayList<Object>();
        exceptionalEventCount = params.getEventCount();
        expectedExceptionalEventCount = exceptionalEventCount * params.getHandlerCount();
        handlerClass = getHandlerClassForThreadMode();
    }

    @Override
    public void prepareTest() {
        try {
            Constructor<?> constructor = handlerClass.getConstructor(PerfTestHandlerEventBus.class);
            for (int i = 0; i < params.getHandlerCount(); i++) {
                Object handler = constructor.newInstance(this);
                handlers.add(handler);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getHandlerClassForThreadMode() {
        switch (params.getThreadMode()) {
            case MAIN:
                return HandleClassEventBusMain.class;
            case MAIN_ORDERED:
                return HandleClassEventBusMainOrdered.class;
            case BACKGROUND:
                return HandleClassEventBusBackground.class;
            case ASYNC:
                return HandlerClassEventBusAsync.class;
            case THROWING:
                return HandleClassEventBusDefault.class;
            default:
                throw new RuntimeException("Unknown: " + params.getThreadMode());
        }
    }

    private static String getDisplayModifier(TestHandlerParams params) {
        String inheritance = params.isExceptionalEventInheritance() ? "" : ", no exceptional event inheritance";
        String ignoreIndex = params.isIgnoreGeneratedIndex() ? ", ignore index" : "";
        return inheritance + ignoreIndex;
    }


    public static class Throw extends PerfTestHandlerEventBus {
        public Throw(Context context, TestHandlerParams params) {
            super(context, params);
        }

        @Override
        public void prepareTest() {
            super.prepareTest();
            super.registerHandlers();
        }

        public void runTest() {
            TestExceptionalEvent exceptionalEvent = new TestExceptionalEvent();
            long timeStart = System.nanoTime();
            for (int i = 0; i < super.exceptionalEventCount; i++) {
                super.eventBus.throwException(exceptionalEvent);
                if (canceled) {
                    break;
                }
            }
            long timeAfterPosting = System.nanoTime();
            waitForReceivedExceptionalEventCount(super.expectedExceptionalEventCount);
            long timeAllReceived = System.nanoTime();

            primaryResultMicros = (timeAfterPosting - timeStart) / 1000;
            primaryResultCount = super.expectedExceptionalEventCount;
            long deliveredMicros = (timeAllReceived - timeStart) / 1000;
            int deliveryRate = (int) (primaryResultCount / (deliveredMicros / 1000000d));
            otherTestResults = "Throw and delivery time: " + deliveredMicros + " micros<br/>" + //
                    "Throw and delivery rate: " + deliveryRate + "/s";
        }

        @Override
        public String getDisplayName() {
            return "EventBus Throw Exceptional Events, " + params.getThreadMode() + getDisplayModifier(params);
        }

    }

    public static class RegisterAll extends PerfTestHandlerEventBus {
        public RegisterAll(Context context, TestHandlerParams params) {
            super(context, params);
        }

        public void runTest() {
            super.registerUnregisterOneHandlers();
            long timeNanos = super.registerHandlers();
            primaryResultMicros = timeNanos / 1000;
            primaryResultCount = params.getHandlerCount();
        }

        @Override
        public String getDisplayName() {
            return "EventBus Register Handler, no unregister handler" + getDisplayModifier(params);
        }
    }

    public static class RegisterOneByOne extends PerfTestHandlerEventBus {
        protected Method clearCachesMethod;

        public RegisterOneByOne(Context context, TestHandlerParams params) {
            super(context, params);
        }

        public void runTest() {
            long time = 0;
            if (clearCachesMethod == null) {
                // Skip first registration unless just the first registration is tested
                super.registerUnregisterOneHandlers();
            }
            for (Object handler : super.handlers) {
                if (clearCachesMethod != null) {
                    try {
                        clearCachesMethod.invoke(null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                long beforeRegister = System.nanoTime();
                super.eventBus.registerHandler(handler);
                long afterRegister = System.nanoTime();
                long end = System.nanoTime();
                long timeMeasureOverhead = (end - afterRegister) * 2;
                long timeRegister = end - beforeRegister - timeMeasureOverhead;
                time += timeRegister;
                super.eventBus.unregisterHandler(handler);
                if (canceled) {
                    return;
                }
            }

            primaryResultMicros = time / 1000;
            primaryResultCount = params.getHandlerCount();
        }

        @Override
        public String getDisplayName() {
            return "EventBus Register Handler" + getDisplayModifier(params);
        }
    }

    public static class RegisterFirstTime extends RegisterOneByOne {

        public RegisterFirstTime(Context context, TestHandlerParams params) {
            super(context, params);
            try {
                Class<?> clazz = Class.forName("org.greenrobot.eventbus.HandlerMethodFinder");
                clearCachesMethod = clazz.getDeclaredMethod("clearCaches");
                clearCachesMethod.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDisplayName() {
            return "EventBus Register, first time"+ getDisplayModifier(params);
        }

    }

    public class HandleClassEventBusMain {
        @Handle(threadMode = ExceptionalThreadMode.MAIN)
        public void onExceptionalEventMainThread(TestExceptionalEvent exceptionalEvent) {
            exceptionalEventsReceivedCount.incrementAndGet();
        }

        public void dummy() {
        }

        public void dummy2() {
        }

        public void dummy3() {
        }

        public void dummy4() {
        }

        public void dummy5() {
        }
    }

    public class HandleClassEventBusMainOrdered {
        @Handle(threadMode = ExceptionalThreadMode.MAIN_ORDERED)
        public void onExceptionalEvent(TestExceptionalEvent exceptionalEvent) {
            exceptionalEventsReceivedCount.incrementAndGet();
        }

        public void dummy() {
        }

        public void dummy2() {
        }

        public void dummy3() {
        }

        public void dummy4() {
        }

        public void dummy5() {
        }
    }

    public class HandleClassEventBusBackground {
        @Handle(threadMode = ExceptionalThreadMode.BACKGROUND)
        public void onExceptionalEventBackgroundThread(TestEvent exceptionalEvent) {
            exceptionalEventsReceivedCount.incrementAndGet();
        }

        public void dummy() {
        }

        public void dummy2() {
        }

        public void dummy3() {
        }

        public void dummy4() {
        }

        public void dummy5() {
        }
    }

    public class HandlerClassEventBusAsync {
        @Handle(threadMode = ExceptionalThreadMode.ASYNC)
        public void onExceptionalEventAsync(TestEvent exceptionalEvent) {
            exceptionalEventsReceivedCount.incrementAndGet();
        }

        public void dummy() {
        }

        public void dummy2() {
        }

        public void dummy3() {
        }

        public void dummy4() {
        }

        public void dummy5() {
        }
    }

    private long registerHandlers() {
        long time = 0;
        for (Object handler : handlers) {
            long timeStart = System.nanoTime();
            eventBus.registerHandler(handler);
            long timeEnd = System.nanoTime();
            time += timeEnd - timeStart;
            if (canceled) {
                return 0;
            }
        }
        return time;
    }

    private void registerUnregisterOneHandlers() {
        if (!handlers.isEmpty()) {
            Object handler = handlers.get(0);
            eventBus.registerHandler(handler);
            eventBus.unregisterHandler(handler);
        }
    }
}
