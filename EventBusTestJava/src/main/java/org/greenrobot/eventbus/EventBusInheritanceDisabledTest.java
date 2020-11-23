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

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusInheritanceDisabledTest {

    protected EventBus eventBus;

    /** Common flow */
    protected int countMyEventExtended;
    protected int countMyEvent;
    protected int countObjectEvent;
    private int countMyEventInterface;
    private int countMyEventInterfaceExtended;

    /** Exceptional flow */
    protected int countMyExceptionalEventExtended;
    protected int countMyExceptionalEvent;
    protected int countObjectExceptionalEvent;
    private int countMyExceptionalEventInterface;
    private int countMyExceptionalEventInterfaceExtended;

    @Before
    public void setUp() throws Exception {
        eventBus = EventBus.builder()
                .eventInheritance(false)
                .exceptionalEventInheritance(false)
                .build();
    }

    @Test
    public void testEventClassHierarchy() {
        eventBus.registerSubscriber(this);

        eventBus.post("Hello");
        assertEquals(0, countObjectEvent);

        eventBus.post(new MyEvent());
        assertEquals(0, countObjectEvent);
        assertEquals(1, countMyEvent);

        eventBus.post(new MyEventExtended());
        assertEquals(0, countObjectEvent);
        assertEquals(1, countMyEvent);
        assertEquals(1, countMyEventExtended);
    }

    @Test
    public void testExceptionalEventClassHierarchy() {
        eventBus.registerHandler(this);

        eventBus.throwException("Hello");
        assertEquals(0, countObjectExceptionalEvent);

        eventBus.throwException(new MyExceptionalEvent());
        assertEquals(0, countObjectExceptionalEvent);
        assertEquals(1, countMyExceptionalEvent);

        eventBus.throwException(new MyExceptionalEventExtended());
        assertEquals(0, countObjectExceptionalEvent);
        assertEquals(1, countMyExceptionalEvent);
        assertEquals(1, countMyExceptionalEventExtended);
    }

    @Test
    public void testEventClassHierarchySticky() {
        eventBus.postSticky("Hello");
        eventBus.postSticky(new MyEvent());
        eventBus.postSticky(new MyEventExtended());
        eventBus.registerSubscriber(new StickySubscriber());
        assertEquals(1, countMyEventExtended);
        assertEquals(1, countMyEvent);
        assertEquals(0, countObjectEvent);
    }

    @Test
    public void testExceptionalEventClassHierarchySticky() {
        eventBus.throwSticky("Hello");
        eventBus.throwSticky(new MyExceptionalEvent());
        eventBus.throwSticky(new MyExceptionalEventExtended());
        eventBus.registerHandler(new StickyHandler());
        assertEquals(1, countMyExceptionalEventExtended);
        assertEquals(1, countMyExceptionalEvent);
        assertEquals(0, countObjectExceptionalEvent);
    }

    @Test
    public void testEventInterfaceHierarchy() {
        eventBus.registerSubscriber(this);

        eventBus.post(new MyEvent());
        assertEquals(0, countMyEventInterface);

        eventBus.post(new MyEventExtended());
        assertEquals(0, countMyEventInterface);
        assertEquals(0, countMyEventInterfaceExtended);
    }

    @Test
    public void testExceptionalEventInterfaceHierarchy() {
        eventBus.registerHandler(this);

        eventBus.throwException(new MyExceptionalEvent());
        assertEquals(0, countMyExceptionalEventInterface);

        eventBus.throwException(new MyExceptionalEventExtended());
        assertEquals(0, countMyExceptionalEventInterface);
        assertEquals(0, countMyExceptionalEventInterfaceExtended);
    }

    @Test
    public void testEventSuperInterfaceHierarchy() {
        eventBus.registerSubscriber(this);

        eventBus.post(new MyEventInterfaceExtended() {
        });
        assertEquals(0, countMyEventInterface);
        assertEquals(0, countMyEventInterfaceExtended);
    }

    @Test
    public void testExceptionalEventSuperInterfaceHierarchy() {
        eventBus.registerHandler(this);

        eventBus.throwException(new MyExceptionalEventInterfaceExtended() {
        });
        assertEquals(0, countMyExceptionalEventInterface);
        assertEquals(0, countMyExceptionalEventInterfaceExtended);
    }

    @Test
    public void testSubscriberClassHierarchy() {
        EventBusInheritanceDisabledSubclassTest
                subscriber = new EventBusInheritanceDisabledSubclassTest();
        eventBus.registerSubscriber(subscriber);

        eventBus.post("Hello");
        assertEquals(0, subscriber.countObjectEvent);

        eventBus.post(new MyEvent());
        assertEquals(0, subscriber.countObjectEvent);
        assertEquals(0, subscriber.countMyEvent);
        assertEquals(1, subscriber.countMyEventOverwritten);

        eventBus.post(new MyEventExtended());
        assertEquals(0, subscriber.countObjectEvent);
        assertEquals(0, subscriber.countMyEvent);
        assertEquals(1, subscriber.countMyEventExtended);
        assertEquals(1, subscriber.countMyEventOverwritten);
    }

    @Test
    public void testHandlerClassHierarchy() {
        EventBusInheritanceDisabledSubclassTest
                handler = new EventBusInheritanceDisabledSubclassTest();
        eventBus.registerHandler(handler);

        eventBus.throwException("Hello");
        assertEquals(0, handler.countObjectExceptionalEvent);

        eventBus.throwException(new MyExceptionalEvent());
        assertEquals(0, handler.countObjectExceptionalEvent);
        assertEquals(0, handler.countMyExceptionalEvent);
        assertEquals(1, handler.countMyExceptionalEventOverwritten);

        eventBus.throwException(new MyExceptionalEventExtended());
        assertEquals(0, handler.countObjectExceptionalEvent);
        assertEquals(0, handler.countMyExceptionalEvent);
        assertEquals(1, handler.countMyExceptionalEventExtended);
        assertEquals(1, handler.countMyExceptionalEventOverwritten);
    }

    @Test
    public void testSubscriberClassHierarchyWithoutNewSubscriberMethod() {
        EventBusInheritanceDisabledSubclassNoMethod
                subscriber = new EventBusInheritanceDisabledSubclassNoMethod();
        eventBus.registerSubscriber(subscriber);

        eventBus.post("Hello");
        assertEquals(0, subscriber.countObjectEvent);

        eventBus.post(new MyEvent());
        assertEquals(0, subscriber.countObjectEvent);
        assertEquals(1, subscriber.countMyEvent);

        eventBus.post(new MyEventExtended());
        assertEquals(0, subscriber.countObjectEvent);
        assertEquals(1, subscriber.countMyEvent);
        assertEquals(1, subscriber.countMyEventExtended);
    }

    @Test
    public void testHandlerClassHierarchyWithoutNewHandlerMethod() {
        EventBusInheritanceDisabledSubclassNoMethod
                handler = new EventBusInheritanceDisabledSubclassNoMethod();
        eventBus.registerHandler(handler);

        eventBus.throwException("Hello");
        assertEquals(0, handler.countObjectExceptionalEvent);

        eventBus.throwException(new MyExceptionalEvent());
        assertEquals(0, handler.countObjectExceptionalEvent);
        assertEquals(1, handler.countMyExceptionalEvent);

        eventBus.throwException(new MyExceptionalEventExtended());
        assertEquals(0, handler.countObjectExceptionalEvent);
        assertEquals(1, handler.countMyExceptionalEvent);
        assertEquals(1, handler.countMyExceptionalEventExtended);
    }

    /** Common flow */

    @Subscribe
    public void onEvent(Object event) {
        countObjectEvent++;
    }

    @Subscribe
    public void onEvent(MyEvent event) {
        countMyEvent++;
    }

    @Subscribe
    public void onEvent(MyEventExtended event) {
        countMyEventExtended++;
    }

    @Subscribe
    public void onEvent(MyEventInterface event) {
        countMyEventInterface++;
    }

    @Subscribe
    public void onEvent(MyEventInterfaceExtended event) {
        countMyEventInterfaceExtended++;
    }

    public static interface MyEventInterface {
    }

    public static class MyEvent implements MyEventInterface {
    }

    public static interface MyEventInterfaceExtended extends MyEventInterface {
    }

    public static class MyEventExtended extends MyEvent implements MyEventInterfaceExtended {
    }

    public class StickySubscriber {
        @Subscribe(sticky = true)
        public void onEvent(Object event) {
            countObjectEvent++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEvent event) {
            countMyEvent++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventExtended event) {
            countMyEventExtended++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventInterface event) {
            countMyEventInterface++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventInterfaceExtended event) {
            countMyEventInterfaceExtended++;
        }
    }

    /** Exceptional flow */

    @Handle
    public void onExceptionalEvent(Object exceptionalEvent) {
        countObjectExceptionalEvent++;
    }

    @Handle
    public void onExceptionalEvent(MyExceptionalEvent exceptionalEvent) {
        countMyExceptionalEvent++;
    }

    @Handle
    public void onExceptionalEvent(MyExceptionalEventExtended exceptionalEvent) {
        countMyExceptionalEventExtended++;
    }

    @Handle
    public void onExceptionalEvent(MyExceptionalEventInterface exceptionalEvent) {
        countMyExceptionalEventInterface++;
    }

    @Handle
    public void onExceptionalEvent(MyExceptionalEventInterfaceExtended exceptionalEvent) {
        countMyExceptionalEventInterfaceExtended++;
    }

    public static interface MyExceptionalEventInterface {
    }

    public static class MyExceptionalEvent implements MyExceptionalEventInterface {
    }

    public static interface MyExceptionalEventInterfaceExtended extends MyExceptionalEventInterface {
    }

    public static class MyExceptionalEventExtended extends MyExceptionalEvent implements MyExceptionalEventInterfaceExtended {
    }

    public class StickyHandler {
        @Handle(sticky = true)
        public void onExceptionalEvent(Object exceptionalEvent) {
            countObjectExceptionalEvent++;
        }

        @Handle(sticky = true)
        public void onExceptionalEvent(MyExceptionalEvent exceptionalEvent) {
            countMyExceptionalEvent++;
        }

        @Handle(sticky = true)
        public void onExceptionalEvent(MyExceptionalEventExtended exceptionalEvent) {
            countMyExceptionalEventExtended++;
        }

        @Handle(sticky = true)
        public void onExceptionalEvent(MyExceptionalEventInterface exceptionalEvent) {
            countMyExceptionalEventInterface++;
        }

        @Handle(sticky = true)
        public void onExceptionalEvent(MyExceptionalEventInterfaceExtended exceptionalEvent) {
            countMyExceptionalEventInterfaceExtended++;
        }
    }
}
