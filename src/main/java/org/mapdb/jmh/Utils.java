package org.mapdb.jmh;

import sun.misc.Unsafe;

import java.util.logging.Level;

/**
 * Created by jan on 1/22/15.
 */
public class Utils {

    @SuppressWarnings("restriction")
    public static sun.misc.Unsafe getUnsafe() {
        try {
            java.lang.reflect.Field singleoneInstanceField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            sun.misc.Unsafe ret =  (sun.misc.Unsafe)singleoneInstanceField.get(null);
            return ret;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
