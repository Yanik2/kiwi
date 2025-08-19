package com.kiwi.persistent;

import java.util.HashMap;
import java.util.Map;

public class PersistentModule {

    public static Storage create() {
        return new Storage();
    }

}
