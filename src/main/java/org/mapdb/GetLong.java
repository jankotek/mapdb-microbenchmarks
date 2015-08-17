package org.mapdb;

import org.openjdk.jmh.annotations.*;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/** test best method to read long from an byte[] */
@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class GetLong {

    static final byte[] b = new byte[10];
    static final ByteBuffer bb = ByteBuffer.wrap(b);
    static final Unsafe unsafe = Utils.getUnsafe();
    static final long unsafeBaseOffset = unsafe.arrayBaseOffset(byte[].class);

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
    public long cycle() {
        return getLong(b, 1);
    }


    public static long getLong2(byte[] buf, int pos) {
        return  ((((long)buf[pos++]) << 56) |
                (((long)buf[pos++] & 0xFF) << 48) |
                (((long)buf[pos++] & 0xFF) << 40) |
                (((long)buf[pos++] & 0xFF) << 32) |
                (((long)buf[pos++] & 0xFF) << 24) |
                (((long)buf[pos++] & 0xFF) << 16) |
                (((long)buf[pos++] & 0xFF) <<  8) |
                (((long)buf[pos] & 0xFF)));
    }

    @Benchmark
    public long  linear() {
        return getLong2(b, 1);
    }



    public static long getLong2_reverse(byte[] buf, int pos) {
        return
                (((long)buf[pos++] & 0xFF)) |
                (((long)buf[pos++] & 0xFF) <<  8) |
                (((long)buf[pos++] & 0xFF) << 16) |
                (((long)buf[pos++] & 0xFF) << 24) |
                (((long)buf[pos++] & 0xFF) << 32) |
                (((long)buf[pos++] & 0xFF) << 40) |
                (((long)buf[pos++] & 0xFF) << 48) |
                ((((long)buf[pos]) << 56));
    }

    @Benchmark
    public long  linear_reverse() {
        return getLong2_reverse(b, 1);
    }


    public static long getLong2_reverse_noinc(byte[] buf, int pos) {
        return
                        (((long)buf[pos] & 0xFF)) |
                        (((long)buf[pos+1] & 0xFF) <<  8) |
                        (((long)buf[pos+2] & 0xFF) << 16) |
                        (((long)buf[pos+3] & 0xFF) << 24) |
                        (((long)buf[pos+4] & 0xFF) << 32) |
                        (((long)buf[pos+5] & 0xFF) << 40) |
                        (((long)buf[pos+6] & 0xFF) << 48) |
                        ((((long)buf[pos+7]) << 56));
    }

    @Benchmark
    public long linear_reverse_noinc() {
        return getLong2_reverse_noinc(b, 1);
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
    public long linear_double_int() {
        return getLong3(b, 1);
    }



    public static long getLong5(byte[] buf, int pos) {
        return
                        ((((long)buf[pos++]) << 56) +
                        (((long)buf[pos++] & 0xFF) << 48) +
                        (((long)buf[pos++] & 0xFF) << 40) +
                        (((long)buf[pos++] & 0xFF) << 32) +
                        (((long)buf[pos++] & 0xFF) << 24) +
                        (((long)buf[pos++] & 0xFF) << 16) +
                        (((long)buf[pos++] & 0xFF) <<  8) +
                        (((long)buf[pos] & 0xFF)));

    }
    @Benchmark
    public long linear_plus() {
        return getLong5(b, 1);
    }



    public static long getLong5_reverse(byte[] buf, int pos) {
        return
                (((long)buf[pos++] & 0xFF))+
                (((long)buf[pos++] & 0xFF) <<  8) +
                (((long)buf[pos++] & 0xFF) << 16) +
                (((long)buf[pos++] & 0xFF) << 24) +
                (((long)buf[pos++] & 0xFF) << 32) +
                (((long)buf[pos++] & 0xFF) << 40) +
                (((long)buf[pos++] & 0xFF) << 48) +
                (((long)buf[pos]) << 56);
    }
    @Benchmark
    public long  linear_plus_reverse() {
        return getLong5_reverse(b, 1);
    }



    @Benchmark
    public long unsafe() {
        return unsafe.getLong(b, unsafeBaseOffset + 1);
    }

    @Benchmark
    public long unsafe_reverse_bytes() {
        long r = unsafe.getLong(b, unsafeBaseOffset + 1);
        return Long.reverseBytes(r);
    }


    @Benchmark
    public long bytebuffer() {
        return bb.getLong(1);
    }



}
