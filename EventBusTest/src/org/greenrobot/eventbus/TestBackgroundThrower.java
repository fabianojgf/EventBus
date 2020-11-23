package org.greenrobot.eventbus;

import java.util.ArrayList;
import java.util.List;

public class TestBackgroundThrower extends Thread {
    private final EventBus eventBus;
    volatile boolean running = true;
    private final List<Object> exceptionalEventQ = new ArrayList<>();
    private final List<Object> exceptionalEventsDone = new ArrayList<>();

    TestBackgroundThrower(EventBus eventBus) {
        super("BackgroundThrower");
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        while (running) {
            Object exceptionalEvent = pollEvent();
            if (exceptionalEvent != null) {
                eventBus.throwException(exceptionalEvent);
                synchronized (exceptionalEventsDone) {
                    exceptionalEventsDone.add(exceptionalEvent);
                    exceptionalEventsDone.notifyAll();
                }
            }
        }
    }

    private synchronized Object pollEvent() {
        Object exceptionalEvent = null;
        synchronized (exceptionalEventQ) {
            if (exceptionalEventQ.isEmpty()) {
                try {
                    exceptionalEventQ.wait(1000);
                } catch (InterruptedException ignored) {
                }
            }
            if(!exceptionalEventQ.isEmpty()) {
                exceptionalEvent = exceptionalEventQ.remove(0);
            }
        }
        return exceptionalEvent;
    }

    void shutdown() {
        running = false;
        synchronized (exceptionalEventQ) {
            exceptionalEventQ.notifyAll();
        }
    }

    void throwException(Object exceptionalEvent) {
        synchronized (exceptionalEventQ) {
            exceptionalEventQ.add(exceptionalEvent);
            exceptionalEventQ.notifyAll();
        }
        synchronized (exceptionalEventsDone) {
            while (!exceptionalEventsDone.remove(exceptionalEvent)) {
                try {
                    exceptionalEventsDone.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
