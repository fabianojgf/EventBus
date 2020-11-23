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

import org.greenrobot.eventbus.meta.HandlerInfo;
import org.greenrobot.eventbus.meta.HandlerInfoIndex;
import org.greenrobot.eventbus.meta.HandlerMethodInfo;
import org.greenrobot.eventbus.meta.SimpleHandlerInfo;
import org.greenrobot.eventbus.meta.SimpleSubscriberInfo;
import org.greenrobot.eventbus.meta.SubscriberInfo;
import org.greenrobot.eventbus.meta.SubscriberInfoIndex;
import org.greenrobot.eventbus.meta.SubscriberMethodInfo;
import org.junit.Assert;
import org.junit.Test;

public class EventBusIndexTest {
    private String value;

    /** Ensures the index is actually used and no reflection fall-back kicks in. */
    @Test
    public void testManualIndexWithoutAnnotationForSubscriber() {
        SubscriberInfoIndex index = new SubscriberInfoIndex() {

            @Override
            public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {
                Assert.assertEquals(EventBusIndexTest.class, subscriberClass);
                SubscriberMethodInfo[] methodInfos = {
                        new SubscriberMethodInfo("someMethodWithoutAnnotation", String.class)
                };
                return new SimpleSubscriberInfo(EventBusIndexTest.class, false, methodInfos);
            }
        };

        EventBus eventBus = EventBus.builder().addIndex(index).build();
        eventBus.registerSubscriber(this);
        eventBus.post("Yepp");
        eventBus.unregisterSubscriber(this);
        Assert.assertEquals("Yepp", value);
    }

    /** Ensures the index is actually used and no reflection fall-back kicks in. */
    @Test
    public void testManualIndexWithoutAnnotationForHandler() {
        HandlerInfoIndex index = new HandlerInfoIndex() {

            @Override
            public HandlerInfo getHandlerInfo(Class<?> handlerClass) {
                Assert.assertEquals(EventBusIndexTest.class, handlerClass);
                HandlerMethodInfo[] methodInfos = {
                        new HandlerMethodInfo("someMethodWithoutAnnotation", String.class)
                };
                return new SimpleHandlerInfo(EventBusIndexTest.class, false, methodInfos);
            }
        };

        EventBus eventBus = EventBus.builder().addIndex(index).build();
            eventBus.registerHandler(this);
        eventBus.throwException("Yepp");
        eventBus.unregisterHandler(this);
        Assert.assertEquals("Yepp", value);
    }

    public void someMethodWithoutAnnotation(String value) {
        this.value = value;
    }
}
