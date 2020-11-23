package org.greenrobot.eventbus;

import org.junit.Ignore;

// Need to use upper class or Android test runner does not pick it up
public class EventBusInheritanceDisabledSubclassTest extends EventBusInheritanceDisabledTest {

    int countMyEventOverwritten;
    int countMyExceptionalEventOverwritten;

    @Subscribe
    public void onEvent(MyEvent event) {
        countMyEventOverwritten++;
    }

    @Handle
    public void onExceptionalEvent(MyExceptionalEvent exceptionalEvent) {
        countMyExceptionalEventOverwritten++;
    }

    @Override
    @Ignore
    public void testEventClassHierarchy() {
        // TODO fix test in super, then remove this
    }
}