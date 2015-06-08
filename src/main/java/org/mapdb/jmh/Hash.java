package org.mapdb.jmh;

import org.openjdk.jmh.annotations.*;

/**
 * Int and Long hashing methods
 */
@State(Scope.Thread)
public class Hash {

    final long t1 = System.nanoTime();
    final int t2 = (int) System.nanoTime();

    {
        System.out.println(Integer.toBinaryString(-1640531527));
        System.out.println(Long.toBinaryString(-7046029254386353131L));
    }
    @Benchmark public long longHash(){
        long t = t1;
        //use cycle with recursive hash to prevent compiler from caching hash results
        for(int i=0;i<10000;i++){
            t = longHash(t);
        }
        return t;
    }

    @Benchmark public long longHashKoloboke(){
        long t = t1;
        for(int i=0;i<10000;i++){
            t = longHashKoloboke(t);
        }
        return t;
    }

    @Benchmark public int intHash(){
        int t = t2;
        for(int i=0;i<10000;i++){
            t = intHash(t);
        }
        return t;
    }

    @Benchmark public long intHashKoloboke(){
        int t = t2;
        for(int i=0;i<10000;i++){
            t = intHashKoloboke(t);
        }
        return t;
    }

    @Benchmark public long intHashSpread(){
        int t = t2;
        for(int i=0;i<10000;i++){
            t = hashSpread(t);
        }
        return t;
    }


    public static int longHashKoloboke(final long key) {
        long h = key * -7046029254386353131L;
        h ^= h >> 32;
        return (int)(h ^ h >> 16);

    }




    public static int longHash(final long key) {
        //$DELAY$
        int h = (int)(key ^ (key >>> 32));
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }


    public static int intHashKoloboke(int key) {
        int h = key * -1640531527;
        return h ^ h >> 16;
    }

    public static int intHash(int h) {
        //$DELAY$
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }


    protected static final  int hashSpread( int h) {
        //spread low bits,
        //need so many mixes so each bit becomes part of segment
        //segment is upper 4 bits
        h ^= (h<<4);
        h ^= (h<<4);
        h ^= (h<<4);
        h ^= (h<<4);
        h ^= (h<<4);
        h ^= (h<<4);
        h ^= (h<<4);

        return h;
    }

}
