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

package org.greenrobot.eventbusperf;

import org.greenrobot.eventbus.ExceptionalThreadMode;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;

public class TestHandlerParams implements Serializable {
    private static final long serialVersionUID = -2739435088947740809L;

    private int exceptionalEventCount;
    private int handlerCount;
    private int throwerCount;
    private ExceptionalThreadMode threadMode;
    private boolean exceptionalEventInheritance;
    private boolean ignoreGeneratedIndex;
    private int testNumber;
    private ArrayList<Class<? extends TestHandler>> testClasses;

    public int getEventCount() {
        return exceptionalEventCount;
    }

    public void setEventCount(int iterations) {
        this.exceptionalEventCount = iterations;
    }

    public int getHandlerCount() {
        return handlerCount;
    }

    public void setHandlerCount(int handlerCount) {
        this.handlerCount = handlerCount;
    }

    public int getThrowerCount() {
        return throwerCount;
    }

    public void setThrowerCount(int throwerCount) {
        this.throwerCount = throwerCount;
    }

    public ExceptionalThreadMode getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(ExceptionalThreadMode threadMode) {
        this.threadMode = threadMode;
    }

    public boolean isExceptionalEventInheritance() {
        return exceptionalEventInheritance;
    }

    public void setExceptionalEventInheritance(boolean exceptionalEventInheritance) {
        this.exceptionalEventInheritance = exceptionalEventInheritance;
    }

    public boolean isIgnoreGeneratedIndex() {
        return ignoreGeneratedIndex;
    }

    public void setIgnoreGeneratedIndex(boolean ignoreGeneratedIndex) {
        this.ignoreGeneratedIndex = ignoreGeneratedIndex;
    }

    public ArrayList<Class<? extends TestHandler>> getTestClasses() {
        return testClasses;
    }

    public void setTestClasses(ArrayList<Class<? extends TestHandler>> testClasses) {
        this.testClasses = testClasses;
    }

    public int getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(int testNumber) {
        this.testNumber = testNumber;
    }

}
