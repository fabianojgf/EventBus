package org.greenrobot.eventbus;

import java.util.ArrayList;
import java.util.List;

/** Helper class used by test inside a jar. */
public class HandlerInJar {
    List<String> collectedStrings = new ArrayList<String>();

    @Handle
    public void collectString(String string) {
        collectedStrings.add(string);
    }

    public List<String> getCollectedStrings() {
        return collectedStrings;
    }
}