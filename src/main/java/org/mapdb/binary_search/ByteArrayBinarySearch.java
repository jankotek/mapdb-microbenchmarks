package org.mapdb.binary_search;


import com.google.common.primitives.Longs;
import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;

import static org.mapdb.Utils.getUnsafe;

@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class ByteArrayBinarySearch {

    static final int count = 2048;

    static final sun.misc.Unsafe UNSAFE = getUnsafe();

    static final ByteBuffer buf = ByteBuffer.allocateDirect(count * 8);

    static final byte[] key = Longs.toByteArray(count / 3);

    static {
        for (int i = 0; i < count; i++) {
            buf.putLong(i * 8, i);
        }
    }


    private static long[] parseKey(byte[] bb) {
        int size = (int) Math.ceil(1.0 * bb.length / 7);
        long[] r = new long[size];
        for (int i = 0; i < size; i++) {
            int offset = Math.min(bb.length - 7, i * 7);
            r[i] = getLong7(bb, offset);
        }
        return r;
    }

    private static long getLong7(byte[] buf, int pos) {
        return
                ((long) (buf[pos++] & 0xff) << 48) |
                        ((long) (buf[pos++] & 0xff) << 40) |
                        ((long) (buf[pos++] & 0xff) << 32) |
                        ((long) (buf[pos++] & 0xff) << 24) |
                        ((long) (buf[pos++] & 0xff) << 16) |
                        ((long) (buf[pos++] & 0xff) << 8) |
                        ((long) (buf[pos] & 0xff));
    }



    private static long getLong8(byte[] buf, int pos) {
        return
                ((long) (buf[pos++] & 0xff) << 56) |
                ((long) (buf[pos++] & 0xff) << 48) |
                        ((long) (buf[pos++] & 0xff) << 40) |
                        ((long) (buf[pos++] & 0xff) << 32) |
                        ((long) (buf[pos++] & 0xff) << 24) |
                        ((long) (buf[pos++] & 0xff) << 16) |
                        ((long) (buf[pos++] & 0xff) << 8) |
                        ((long) (buf[pos] & 0xff));
    }

    static int unsafeBinarySearch(ByteBuffer keys, byte[] key) {
        long bufAddress = ((sun.nio.ch.DirectBuffer) keys).address();

        long[] keyParsed = parseKey(key);
        int keySize = key.length;

        int lo = 0;
        int hi = count;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int comp = unsafeCompare(bufAddress, mid, keySize, keyParsed);
            if (comp < 0)
                lo = mid + 1;
            else if (comp > 0)
                hi = mid - 1;
            else
                return mid;
        }
        return -1;
    }


    private static long unsafeGetLong(long address) {
        long l = UNSAFE.getLong(address);
        return Long.reverseBytes(l);
    }

    private static int unsafeCompare(long bufAddress, int mid, int keySize, long[] keyParsed) {
        bufAddress = bufAddress + mid * (keySize + 4 + 8);
        int offset = -1;
        for (long keyPart : keyParsed) {
            long v = unsafeGetLong(bufAddress + offset) & 0xFFFFFFFFFFFFFFL;
            if (v < keyPart)
                return -1;
            else if (v > keyPart)
                return 1;
            offset = Math.min(keySize - 8, offset + 7); //TODO this does not work with small keys
        }
        return 0;
    }

    @Benchmark
    public int search_Unsafe_Signed() {
        return unsafeBinarySearch(buf, key);
    }


    @Benchmark
    public int search_Unsafe_unsigned() {
        return unsafeBinarySearch_unsigned(buf, key);
    }


    static int unsafeBinarySearch_unsigned(ByteBuffer keys, byte[] key) {
        long bufAddress = ((sun.nio.ch.DirectBuffer) keys).address();

        long[] keyParsed = parseKey_unsigned(key);
        int keySize = key.length;

        int lo = 0;
        int hi = count;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int comp = unsafeCompare_unsigned(bufAddress, mid, keySize, keyParsed);
            if (comp < 0)
                lo = mid + 1;
            else if (comp > 0)
                hi = mid - 1;
            else
                return mid;
        }
        return -1;
    }


    private static int unsafeCompare_unsigned(long bufAddress, int mid, int keySize, long[] keyParsed) {
        bufAddress = bufAddress + mid * (keySize + 4 + 8);
        int offset = 0;
        for (long keyPart : keyParsed) {
            long v = unsafeGetLong(bufAddress + offset);
            int comp = Long.compareUnsigned(v, keyPart);
            if (comp!=0)
                return comp;
            offset = Math.min(keySize - 8, offset + 8); //TODO this does not work with small keys
        }
        return 0;
    }



    private static long[] parseKey_unsigned(byte[] bb) {
        int size = (int) Math.ceil(1.0 * bb.length / 8);
        long[] r = new long[size];
        for (int i = 0; i < size; i++) {
            int offset = Math.min(bb.length - 8, i * 8);
            r[i] = getLong8(bb, offset);
        }
        return r;
    }

}