package org.mapdb.jmh;


import net.jpountz.util.UnsafeUtils;
import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

/** measures parity 1,2... */
@State(Scope.Thread)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
public class Parity {


    public static long parity1Set(long i) {
        return i | ((Long.bitCount(i)+1)%2);
    }

    public static long parity1Get(long i) {
        if(Long.bitCount(i)%2!=1){
            throw new RuntimeException();
        }
        return i&0xFFFFFFFFFFFFFFFEL;
    }

    public static long parity3Set(long i) {
        return i | ((Long.bitCount(i)+1)%8);
    }

    public static long parity3Get(long i) {
        long ret = i&0xFFFFFFFFFFFFFFF8L;
        if((Long.bitCount(ret)+1)%8!=(i&0x7)){
            throw new RuntimeException();
        }
        return ret;
    }

    public static long parity4Set(long i) {
        return i | ((Long.bitCount(i)+1)%16);
    }

    public static long parity4Get(long i) {
        long ret = i&0xFFFFFFFFFFFFFFF0L;
        if((Long.bitCount(ret)+1)%16!=(i&0xF)){
            throw new RuntimeException();
        }
        return ret;
    }


    public static long parity16Set(long i) {
        return i | (longHash(i)&0xFFFFL);
    }

    public static long parity16Get(long i) {
        long ret = i&0xFFFFFFFFFFFF0000L;
        if((longHash(ret)&0xFFFFL) != (i&0xFFFFL)){
            throw new RuntimeException();
        }
        return ret;
    }


    public static int longHash(final long key) {
        //$DELAY$
        int h = (int)(key ^ (key >>> 32));
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }


    @Benchmark public long parity1(){
        return parity1Get(parity1Set(0xFFFFF00000L));
    }

    @Benchmark public long parity3(){
        return parity3Get(parity3Set(0xFFFFF00000L));
    }
    @Benchmark public long parity4(){
        return parity4Get(parity4Set(0xFFFFF00000L));
    }
    @Benchmark public long parity8(){
        return parity16Get(parity16Set(0xFFFFF00000L));
    }
}
