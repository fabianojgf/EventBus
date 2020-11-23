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

import android.app.Activity;
import android.content.Context;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import org.greenrobot.eventbusperf.TestExceptionalEvent;
import org.greenrobot.eventbusperf.TestHandler;
import org.greenrobot.eventbusperf.TestEvent;
import org.greenrobot.eventbusperf.TestHandlerParams;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PerfTestHandlerOtto extends TestHandler {

    private final Bus eventBus;
    private final ArrayList<Object> handlers;
    private final Class<?> handlerClass;
    private final int exceptionalEventCount;
    private final int expectedExceptionalEventCount;

    public PerfTestHandlerOtto(Context context, TestHandlerParams params) {
        super(context, params);
        eventBus = new Bus(ThreadEnforcer.ANY);
        handlers = new ArrayList<Object>();
        exceptionalEventCount = params.getEventCount();
        expectedExceptionalEventCount = exceptionalEventCount * params.getHandlerCount();
        handlerClass = Handler.class;
    }

    @Override
    public void prepareTest() {
        Looper.prepare();

        try {
            Constructor<?> constructor = handlerClass.getConstructor(PerfTestHandlerOtto.class);
            for (int i = 0; i < params.getHandlerCount(); i++) {
                Object handler = constructor.newInstance(this);
                handlers.add(handler);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Throw extends PerfTestHandlerOtto {
        public Throw(Context context, TestHandlerParams params) {
            super(context, params);
        }

        @Override
        public void prepareTest() {
            super.prepareTest();
            super.registerHandlers();
        }

        public void runTest() {
            TestExceptionalEvent event = new TestExceptionalEvent();
            long timeStart = System.nanoTime();
            for (int i = 0; i < super.exceptionalEventCount; i++) {
                super.eventBus.post(event);
                if (canceled) {
                    break;
                }
            }
            long timeAfterPosting = System.nanoTime();
            waitForReceivedExceptionalEventCount(super.expectedExceptionalEventCount);

            primaryResultMicros = (timeAfterPosting - timeStart) / 1000;
            primaryResultCount = super.expectedExceptionalEventCount;
        }

        @Override
        public String getDisplayName() {
            return "Otto Throw (Exceptional) Events";
        }
    }

    public static class RegisterAll extends PerfTestHandlerOtto {
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
            return "Otto Register (Handler), no unregister (handler)";
        }
    }

    public static class RegisterOneByOne extends PerfTestHandlerOtto {
        protected Field cacheField;

        public RegisterOneByOne(Context context, TestHandlerParams params) {
            super(context, params);
        }

        @SuppressWarnings("rawtypes")
        public void runTest() {
            long time = 0;
            if (cacheField == null) {
                // Skip first registration unless just the first registration is tested
                super.registerUnregisterOneHandlers();
            }
            for (Object handler : super.handlers) {
                if (cacheField != null) {
                    try {
                        cacheField.set(null, new ConcurrentHashMap());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                long beforeRegister = System.nanoTime();
                super.eventBus.register(handler);

                long afterRegister = System.nanoTime();
                long end = System.nanoTime();
                long timeMeasureOverhead = (end - afterRegister) * 2;
                long timeRegister = end - beforeRegister - timeMeasureOverhead;
                time += timeRegister;
                super.eventBus.unregister(handler);
                if (canceled) {
                    return;
                }
            }

            primaryResultMicros = time / 1000;
            primaryResultCount = params.getHandlerCount();
        }

        @Override
        public String getDisplayName() {
            return "Otto Register (Handler)";
        }
    }

    public static class RegisterFirstTime extends RegisterOneByOne {

        public RegisterFirstTime(Context context, TestHandlerParams params) {
            super(context, params);
            try {
                Class<?> clazz = Class.forName("com.squareup.otto.AnnotatedHandlerFinder");
                cacheField = clazz.getDeclaredField("SUBSCRIBERS_CACHE");
                cacheField.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDisplayName() {
            return "Otto Register (Handler), first time";
        }

    }

    public class Handler extends Activity {
        public Handler() {
        }

        @Subscribe
        public void onEvent(TestExceptionalEvent exceptionalEvent) {
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
            eventBus.register(handler);
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
            eventBus.register(handler);
            eventBus.unregister(handler);
        }
    }
}
