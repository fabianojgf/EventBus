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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class EventBusMultithreadedTest extends AbstractEventBusTest {

    static final int COUNT = LONG_TESTS ? 100000 : 1000;

    /** Common flow */
    final AtomicInteger countStringEvent = new AtomicInteger();
    final AtomicInteger countIntegerEvent = new AtomicInteger();
    final AtomicInteger countObjectEvent = new AtomicInteger();
    final AtomicInteger countIntTestEvent = new AtomicInteger();

    String lastStringEvent;
    Integer lastIntegerEvent;
    IntTest lastEventIntTest;

    /** Exceptional flow */
    final AtomicInteger countStringExceptionalEvent = new AtomicInteger();
    final AtomicInteger countIntegerExceptionalEvent = new AtomicInteger();
    final AtomicInteger countObjectExceptionalEvent = new AtomicInteger();
    final AtomicInteger countIntTestExceptionalEvent = new AtomicInteger();

    String lastStringExceptionalEvent;
    Integer lastIntegerExceptionalEvent;
    IntTest lastExceptionalEventIntTest;

    @Test
    public void testPost01Thread() throws InterruptedException {
        runThreadsSingleEventType(1);
    }

    @Test
    public void testThrow01Thread() throws InterruptedException {
        runThreadsSingleExceptionalEventType(1);
    }

    @Test
    public void testPost04Threads() throws InterruptedException {
        runThreadsSingleEventType(4);
    }

    @Test
    public void testThrow04Threads() throws InterruptedException {
        runThreadsSingleExceptionalEventType(4);
    }

    @Test
    public void testPost40Threads() throws InterruptedException {
        runThreadsSingleEventType(40);
    }

    @Test
    public void testThrow40Threads() throws InterruptedException {
        runThreadsSingleExceptionalEventType(40);
    }

    @Test
    public void testPostMixedEventType01Thread() throws InterruptedException {
        runThreadsMixedEventType(1);
    }

    @Test
    public void testThrowMixedEventType01Thread() throws InterruptedException {
        runThreadsSingleExceptionalEventType(1);
    }

    @Test
    public void testPostMixedEventType04Threads() throws InterruptedException {
        runThreadsMixedEventType(4);
    }

    @Test
    public void testThrowMixedEventType04Threads() throws InterruptedException {
        runThreadsSingleExceptionalEventType(4);
    }

    @Test
    public void testPostMixedEventType40Threads() throws InterruptedException {
        runThreadsMixedEventType(40);
    }

    @Test
    public void testThrowMixedEventType40Threads() throws InterruptedException {
        runThreadsSingleExceptionalEventType(40);
    }

    private void runThreadsSingleEventType(int threadCount) throws InterruptedException {
        int iterations = COUNT / threadCount;
        eventBus.registerSubscriber(this);

        CountDownLatch latch = new CountDownLatch(threadCount + 1);
        List<PosterThread> threads = startPosterThreads(latch, threadCount, iterations, "Hello");
        long time = triggerAndWaitForPosterThreads(threads, latch);

        log(threadCount + " threads posted " + iterations + " events each in " + time + "ms");

        waitForEventCount(COUNT * 2, 5000);

        assertEquals("Hello", lastStringEvent);
        int expectedCount = threadCount * iterations;
        assertEquals(expectedCount, countStringEvent.intValue());
        assertEquals(expectedCount, countObjectEvent.intValue());
    }

    private void runThreadsSingleExceptionalEventType(int threadCount) throws InterruptedException {
        int iterations = COUNT / threadCount;
        eventBus.registerHandler(this);

        CountDownLatch latch = new CountDownLatch(threadCount + 1);
        List<ThrowerThread> threads = startThrowerThreads(latch, threadCount, iterations, "Hello");
        long time = triggerAndWaitForThrowerThreads(threads, latch);

        log(threadCount + " threads throwed " + iterations + " exceptional events each in " + time + "ms");

        waitForExceptionalEventCount(COUNT * 2, 5000);

        assertEquals("Hello", lastStringExceptionalEvent);
        int expectedCount = threadCount * iterations;
        assertEquals(expectedCount, countStringExceptionalEvent.intValue());
        assertEquals(expectedCount, countObjectExceptionalEvent.intValue());
    }

    private void runThreadsMixedEventType(int threadCount) throws InterruptedException {
        runThreadsMixedEventType(COUNT, threadCount);
    }

    private void runThreadsMixedExceptionalEventType(int threadCount) throws InterruptedException {
        runThreadsMixedExceptionalEventType(COUNT, threadCount);
    }

    void runThreadsMixedEventType(int count, int threadCount) throws InterruptedException {
        eventBus.registerSubscriber(this);
        int eventTypeCount = 3;
        int iterations = count / threadCount / eventTypeCount;

        CountDownLatch latch = new CountDownLatch(eventTypeCount * threadCount + 1);
        List<PosterThread> threadsString = startPosterThreads(latch, threadCount, iterations, "Hello");
        List<PosterThread> threadsInteger = startPosterThreads(latch, threadCount, iterations, 42);
        List<PosterThread> threadsIntTestEvent = startPosterThreads(latch, threadCount, iterations, new IntTest(7));

        List<PosterThread> threads = new ArrayList<PosterThread>();
        threads.addAll(threadsString);
        threads.addAll(threadsInteger);
        threads.addAll(threadsIntTestEvent);
        long time = triggerAndWaitForPosterThreads(threads, latch);

        log(threadCount * eventTypeCount + " mixed threads posted " + iterations + " events each in "
                + time + "ms");

        int expectedCountEach = threadCount * iterations;
        int expectedCountTotal = expectedCountEach * eventTypeCount * 2;
        waitForEventCount(expectedCountTotal, 5000);

        assertEquals("Hello", lastStringEvent);
        assertEquals(42, lastIntegerEvent.intValue());
        assertEquals(7, lastEventIntTest.value);

        assertEquals(expectedCountEach, countStringEvent.intValue());
        assertEquals(expectedCountEach, countIntegerEvent.intValue());
        assertEquals(expectedCountEach, countIntTestEvent.intValue());

        assertEquals(expectedCountEach * eventTypeCount, countObjectEvent.intValue());
    }

    void runThreadsMixedExceptionalEventType(int count, int threadCount) throws InterruptedException {
        eventBus.registerHandler(this);
        int exceptionalEventTypeCount = 3;
        int iterations = count / threadCount / exceptionalEventTypeCount;

        CountDownLatch latch = new CountDownLatch(exceptionalEventTypeCount * threadCount + 1);
        List<ThrowerThread> threadsString = startThrowerThreads(latch, threadCount, iterations, "Hello");
        List<ThrowerThread> threadsInteger = startThrowerThreads(latch, threadCount, iterations, 42);
        List<ThrowerThread> threadsIntTestExceptionalEvent = startThrowerThreads(latch, threadCount, iterations, new IntTest(7));

        List<ThrowerThread> threads = new ArrayList<ThrowerThread>();
        threads.addAll(threadsString);
        threads.addAll(threadsInteger);
        threads.addAll(threadsIntTestExceptionalEvent);
        long time = triggerAndWaitForThrowerThreads(threads, latch);

        log(threadCount * exceptionalEventTypeCount + " mixed threads throwed " + iterations + " exceptional events each in "
                + time + "ms");

        int expectedCountEach = threadCount * iterations;
        int expectedCountTotal = expectedCountEach * exceptionalEventTypeCount * 2;
        waitForExceptionalEventCount(expectedCountTotal, 5000);

        assertEquals("Hello", lastStringExceptionalEvent);
        assertEquals(42, lastIntegerExceptionalEvent.intValue());
        assertEquals(7, lastExceptionalEventIntTest.value);

        assertEquals(expectedCountEach, countStringExceptionalEvent.intValue());
        assertEquals(expectedCountEach, countIntegerExceptionalEvent.intValue());
        assertEquals(expectedCountEach, countIntTestExceptionalEvent.intValue());

        assertEquals(expectedCountEach * exceptionalEventTypeCount, countObjectExceptionalEvent.intValue());
    }

    /** Common flow */

    private long triggerAndWaitForPosterThreads(List<PosterThread> threads, CountDownLatch latch) throws InterruptedException {
        while (latch.getCount() != 1) {
            // Let all other threads prepare and ensure this one is the last
            Thread.sleep(1);
        }
        long start = System.currentTimeMillis();
        latch.countDown();
        for (PosterThread thread : threads) {
            thread.join();
        }
        return System.currentTimeMillis() - start;
    }

    private List<PosterThread> startPosterThreads(CountDownLatch latch, int threadCount, int iterations, Object eventToPost) {
        List<PosterThread> threads = new ArrayList<PosterThread>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            PosterThread thread = new PosterThread(latch, iterations, eventToPost);
            thread.start();
            threads.add(thread);
        }
        return threads;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackgroundThread(String event) {
        lastStringEvent = event;
        countStringEvent.incrementAndGet();
        trackEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Integer event) {
        lastIntegerEvent = event;
        countIntegerEvent.incrementAndGet();
        trackEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsync(IntTest event) {
        countIntTestEvent.incrementAndGet();
        lastEventIntTest = event;
        trackEvent(event);
    }

    @Subscribe
    public void onEvent(Object event) {
        countObjectEvent.incrementAndGet();
        trackEvent(event);
    }

    class PosterThread extends Thread {

        private final CountDownLatch startLatch;
        private final int iterations;
        private final Object eventToPost;

        public PosterThread(CountDownLatch latch, int iterations, Object eventToPost) {
            this.startLatch = latch;
            this.iterations = iterations;
            this.eventToPost = eventToPost;
        }

        @Override
        public void run() {
            startLatch.countDown();
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                log("Unexpected interrupt", e);
            }

            for (int i = 0; i < iterations; i++) {
                eventBus.post(eventToPost);
            }
        }
    }

    /** Exceptional flow */

    private long triggerAndWaitForThrowerThreads(List<ThrowerThread> threads, CountDownLatch latch) throws InterruptedException {
        while (latch.getCount() != 1) {
            // Let all other threads prepare and ensure this one is the last
            Thread.sleep(1);
        }
        long start = System.currentTimeMillis();
        latch.countDown();
        for (ThrowerThread thread : threads) {
            thread.join();
        }
        return System.currentTimeMillis() - start;
    }

    private List<ThrowerThread> startThrowerThreads(CountDownLatch latch, int threadCount, int iterations, Object exceptionalEventToThrow) {
        List<ThrowerThread> threads = new ArrayList<ThrowerThread>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            ThrowerThread thread = new ThrowerThread(latch, iterations, exceptionalEventToThrow);
            thread.start();
            threads.add(thread);
        }
        return threads;
    }

    @Handle(threadMode = ExceptionalThreadMode.BACKGROUND)
    public void onExceptionalEventBackgroundThread(String exceptionalEvent) {
        lastStringExceptionalEvent = exceptionalEvent;
        countStringExceptionalEvent.incrementAndGet();
        trackExceptionalEvent(exceptionalEvent);
    }

    @Handle(threadMode = ExceptionalThreadMode.MAIN)
    public void onExceptionalEventMainThread(Integer exceptionalEvent) {
        lastIntegerExceptionalEvent = exceptionalEvent;
        countIntegerExceptionalEvent.incrementAndGet();
        trackExceptionalEvent(exceptionalEvent);
    }

    @Handle(threadMode = ExceptionalThreadMode.ASYNC)
    public void onExceptionalEventAsync(IntTest exceptionalEvent) {
        countIntTestExceptionalEvent.incrementAndGet();
        lastExceptionalEventIntTest = exceptionalEvent;
        trackExceptionalEvent(exceptionalEvent);
    }

    @Handle
    public void onExceptionalEvent(Object exceptionalEvent) {
        countObjectExceptionalEvent.incrementAndGet();
        trackExceptionalEvent(exceptionalEvent);
    }

    class ThrowerThread extends Thread {

        private final CountDownLatch startLatch;
        private final int iterations;
        private final Object exceptionalEventToThrow;

        public ThrowerThread(CountDownLatch latch, int iterations, Object exceptionalEventToThrow) {
            this.startLatch = latch;
            this.iterations = iterations;
            this.exceptionalEventToThrow = exceptionalEventToThrow;
        }

        @Override
        public void run() {
            startLatch.countDown();
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                log("Unexpected interrupt", e);
            }

            for (int i = 0; i < iterations; i++) {
                eventBus.throwException(exceptionalEventToThrow);
            }
        }
    }
}
