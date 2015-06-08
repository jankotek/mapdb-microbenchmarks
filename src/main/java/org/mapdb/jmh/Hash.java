package org.mapdb.jmh;

import org.openjdk.jmh.annotations.*;

/**
 * Int and Long hashing methods
 */
@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class Hash {

    @Benchmark public int longHash1(){
        return longHash(1);
    }

    @Benchmark public  int intHash1(){
        return intHash(1);
    }

    @Benchmark  public int longHash2555(){
        return longHash(2555);
    }

    @Benchmark public  int intHash12555(){
        return intHash(2555);
    }

    @Benchmark  public int longHashMax(){
        return longHash(0xFFFFFFFFFFFFFFFFL);
    }

    @Benchmark public  int intHashMax(){
        return intHash(0xFFFFFFFF);
    }


    @Benchmark  public int longHashKolo1(){
        return longHashKoloboke(1);
    }

    @Benchmark  public int intHashKolo1(){
        return intHashKoloboke(1);
    }

    @Benchmark  public int longHashKolo2555(){
        return longHashKoloboke(2555);
    }

    @Benchmark  public int intHashKolo12555(){
        return intHashKoloboke(2555);
    }



    public static int longHashKoloboke(final long key) {
        long h = key * -7046029254386353131L;
        h ^= h >> 32;
        return (int)(h ^ h >> 16);

    }

    public static int intHashKoloboke(int key) {
        int h = key * -1640531527;
        return h ^ h >> 16;
    }



    public static int longHash(final long key) {
        //$DELAY$
        int h = (int)(key ^ (key >>> 32));
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    public static int intHash(int h) {
        //$DELAY$
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }


}
