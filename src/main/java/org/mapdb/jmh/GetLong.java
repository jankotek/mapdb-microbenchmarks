

package org.mapdb.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.util.Random;

/** test best method to read long from an byte[] */
@State(Scope.Thread)
public class GetLong {

    byte[] b = new byte[10];
    ByteBuffer bb = ByteBuffer.wrap(b);
    Unsafe unsafe = Utils.getUnsafe();
    long unsafeBaseOffset = unsafe.arrayBaseOffset(byte[].class);

    @Setup
    public void init(){
        new Random().nextBytes(b);
    }

    public static long getLong(byte[] buf, int pos) {
        final int end = pos + 8;
        long ret = 0;
        for (; pos < end; pos++) {
            ret = (ret << 8) | (buf[pos] & 0xFF);
        }
        return ret;
    }

    @Benchmark
    public void cycle() {
        getLong(b,1);
    }


    public static long getLong2(byte[] buf, int pos) {
        return
                ((((long)buf[pos++]) << 56) |
                        (((long)buf[pos++] & 0xFF) << 48) |
                        (((long)buf[pos++] & 0xFF) << 40) |
                        (((long)buf[pos++] & 0xFF) << 32) |
                        ((((long)buf[pos++] & 0xFF) << 24) |
                                (((long)buf[pos++] & 0xFF) << 16) |
                                (((long)buf[pos++] & 0xFF) <<  8) |
                                (((long)buf[pos] & 0xFF))));

    }
    @Benchmark
    public void linear() {
        getLong2(b, 1);
    }


    public static long getLong3(byte[] buf, int pos) {
        long v1 =((((long)buf[pos++]) << 56) |
                (((long)buf[pos++] & 0xFF) << 48) |
                (((long)buf[pos++] & 0xFF) << 40) |
                (((long)buf[pos++] & 0xFF) << 32));


        return v1 | (((int)buf[pos++] ) << 24) |
                (((int)buf[pos++] & 0xFF) << 16) |
                (((int)buf[pos++] & 0xFF) <<  8) |
                (((int)buf[pos] & 0xFF));
    }

    @Benchmark
    public void linear_double_int() {
        getLong3(b, 1);
    }



    public static long getLong5(byte[] buf, int pos) {
        return
                ((((long)buf[pos++]) << 56) +
                        (((long)buf[pos++] & 0xFF) << 48) +
                        (((long)buf[pos++] & 0xFF) << 40) +
                        (((long)buf[pos++] & 0xFF) << 32) +
                        ((((long)buf[pos++] & 0xFF) << 24) +
                                (((long)buf[pos++] & 0xFF) << 16) +
                                (((long)buf[pos++] & 0xFF) <<  8) +
                                (((long)buf[pos] & 0xFF))));

    }
    @Benchmark
    public void linear_plus() {
        getLong5(b, 1);
    }



    @Benchmark
    public void unsafe() {
        unsafe.getLong(b, unsafeBaseOffset + 1);
    }

    @Benchmark
    public void bytebuffer() {
        bb.getLong(1);
    }



}
