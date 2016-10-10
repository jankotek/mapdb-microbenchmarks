package org.mapdb;

import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tests various ways to get class
 */

@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class ClassForName {


    static String className = ClassForName.class.getName();
    static ClassLoader loader = Thread.currentThread().getContextClassLoader();
    static Map cached = new ConcurrentHashMap();
    static Map cached2 = new ConcurrentHashMap();

    static{
        cached.put(className, ClassForName.class);
    }


    @Benchmark public int forNameInit() throws ClassNotFoundException {
        return Class.forName(className, true, loader).hashCode();
    }



    @Benchmark public int forNameNoInit() throws ClassNotFoundException {
        return Class.forName(className, false, loader).hashCode();
    }


    @Benchmark public int cached(){
        return cached.get(className).hashCode();
    }


    @Benchmark public int cached2() throws ClassNotFoundException {
        Class c = (Class) cached.get(className);
        if(c == null) {
            c = Class.forName(className, true, loader);
            cached.put(className, c);
        }
        return c.hashCode();
    }
}