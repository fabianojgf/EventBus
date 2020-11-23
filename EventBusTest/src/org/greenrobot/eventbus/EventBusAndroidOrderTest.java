package org.greenrobot.eventbus;

import android.os.Handler;
import android.os.Looper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class EventBusAndroidOrderTest extends AbstractAndroidEventBusTest {

    private TestBackgroundPoster backgroundPoster;
    private TestBackgroundThrower backgroundThrower;
    private Handler handler;

    @Before
    public void setUp() throws Exception {
        handler = new Handler(Looper.getMainLooper());
        /** Common flow */
        backgroundPoster = new TestBackgroundPoster(eventBus);
        backgroundPoster.start();
        /** Exceptional flow */
        backgroundThrower = new TestBackgroundThrower(eventBus);
        backgroundThrower.start();
    }

    @After
    public void tearDown() throws Exception {
        /** Common flow */
        backgroundPoster.shutdown();
        backgroundPoster.join();
        /** Exceptional flow */
        backgroundThrower.shutdown();
        backgroundThrower.join();
    }

    @Test
    public void backgroundPostAndMainUnordered() {
        eventBus.registerSubscriber(this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                // post from non-main thread
                backgroundPoster.post("non-main");
                // post from main thread
                eventBus.post("main");
            }
        });

        waitForEventCount(2, 1000);

        // observe that event from *main* thread is posted FIRST
        // NOT in posting order
        assertEquals("non-main", lastEvent);
    }

    @Test
    public void backgroundThrowAndMainUnordered() {
        eventBus.registerHandler(this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                // throw from non-main thread
                backgroundThrower.throwException("non-main");
                // throw from main thread
                eventBus.throwException("main");
            }
        });

        waitForExceptionalEventCount(2, 1000);

        // observe that exceptional event from *main* thread is throwed FIRST
        // NOT in throwing order
        assertEquals("non-main", lastExceptionalEvent);
    }

    @Test
    public void backgroundPostAndMainOrdered() {
        eventBus.registerSubscriber(this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                // post from non-main thread
                backgroundPoster.post(new OrderedEvent("non-main"));
                // post from main thread
                eventBus.post(new OrderedEvent("main"));
            }
        });

        waitForEventCount(2, 1000);

        // observe that event from *main* thread is posted LAST
        // IN posting order
        assertEquals("main", ((OrderedEvent) lastEvent).thread);
    }

    @Test
    public void backgroundThrowAndMainOrdered() {
        eventBus.registerHandler(this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                // throw from non-main thread
                backgroundThrower.throwException(new OrderedExceptionalEvent("non-main"));
                // throw from main thread
                eventBus.throwException(new OrderedExceptionalEvent("main"));
            }
        });

        waitForExceptionalEventCount(2, 1000);

        // observe that exceptional event from *main* thread is throwed LAST
        // IN throwing order
        assertEquals("main", ((OrderedExceptionalEvent) lastExceptionalEvent).thread);
    }

    /** Common flow */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String event) {
        trackEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEvent(OrderedEvent event) {
        trackEvent(event);
    }

    static class OrderedEvent {
        String thread;

        OrderedEvent(String thread) {
            this.thread = thread;
        }
    }

    /** Exceptional flow */

    @Handle(threadMode = ExceptionalThreadMode.MAIN)
    public void onExceptionalEvent(String exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
    }

    @Handle(threadMode = ExceptionalThreadMode.MAIN_ORDERED)
    public void onExceptionalEvent(OrderedExceptionalEvent exceptionalEvent) {
        trackExceptionalEvent(exceptionalEvent);
    }

    static class OrderedExceptionalEvent {
        String thread;

        OrderedExceptionalEvent(String thread) {
            this.thread = thread;
        }
    }
}
