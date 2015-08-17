package org.mapdb;

import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;

@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 5)

public class StringHash {

    static String s = "239e90fkf239e90fkf239e90fkf239e90fkf239e90fkf239e90fkf239e90fkf239e90fkf";

    public StringHash() {
        try {
            f = String.class.getDeclaredField("value");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        f.setAccessible(true);
    }

    @Benchmark
    public int nat(){
        return s.hashCode();
    }

    @Benchmark
    public int fix(){
        return hashCode(s);
    }

    public int hashCode(String s) {
        int len = s.length();
        int ret = 1;
        for(int i=0;i<len;i++){
            char c = s.charAt(i);
            ret = -1640531527 * ret + c;
        }
        return ret;
    }


    @Benchmark
    public int fix2(){
        return hashCode2(s);
    }

    public int hashCode2(String s) {
        byte[] b = s.getBytes();
        int ret = 1;
        for(byte bb:b){
            ret = -1640531527 * ret + bb;
        }
        return ret;
    }


    @Benchmark
    public int fix3() throws IllegalAccessException {
        return hashCode3(s);
    }

    Field f;


    public int hashCode3(String s) throws IllegalAccessException {
        char[] value = (char[]) f.get(s);
        int ret = 1;
        for(char bb:value){
            ret = -1640531527 * ret + bb;
        }
        return ret;
    }

}
