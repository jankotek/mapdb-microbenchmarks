package org.mapdb.binary_search;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * An effort to improve {@link java.util.Arrays#binarySearch(Object[], int, int, Object)}
 * It tried to elimite one conditions, but improved version is actually slower:
 *
 *
 * <pre>
 * Benchmark                              Mode  Cnt     Score    Error  Units
 * ArrayBinarySearchTest.juArrays        thrpt  200  3515.046 ± 57.335  ops/s
 * ArrayBinarySearchTest.juArrays_clone  thrpt  200  3598.518 ± 61.218  ops/s
 * ArrayBinarySearchTest.optimized       thrpt  200  3280.587 ± 17.517  ops/s
 * </pre>
 */
@State(Scope.Thread)
public class ArrayBinarySearchTest {

    static Object[] vals = new Object[1000];
    static int mid;
    static{
        Integer value = 10;
        for(int i=0;i<vals.length;i++){
            if(value<0)
                throw new AssertionError();
            vals[i] = value;
            if(i==vals.length/2)
                mid = value;
            value += 1 + value/100;
        }
    }


    @Benchmark
    public void juArrays(){
        int mid = ArrayBinarySearchTest.mid;
        Object[] vals = ArrayBinarySearchTest.vals;
        int res = 0;
        for(Integer i=0;i<mid;i++){
            res += Arrays.binarySearch(vals, i);
        }
        if(res==0)
            System.out.println("aa");
    }

    /** cloned from java.util.Arrays for comparation */
    static int binarySearch0(Object[] a, int fromIndex, int toIndex,
                                     Object key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            @SuppressWarnings("rawtypes")
            Comparable midVal = (Comparable)a[mid];
            @SuppressWarnings("unchecked")
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }


    @Benchmark
    public void juArrays_clone(){
        int mid = ArrayBinarySearchTest.mid;
        Object[] vals = ArrayBinarySearchTest.vals;
        int len = vals.length;
        int res = 0;
        for(Integer i=0;i<mid;i++){
            res += binarySearch0(vals, 0, len, i);
        }
        if(res==0)
            System.out.println("aa");
    }


    /** optimized version of binary search */
    static int binarySearch_optimized(Object[] a, int low, int high,
                                     Object key) {
        high -=1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            @SuppressWarnings("rawtypes")
            Comparable midVal = (Comparable)a[mid];
            @SuppressWarnings("unchecked")
            int cmp = midVal.compareTo(key);

            if (cmp == 0)
                return mid;

            // if(midVal<key) cmp=0xFFFFFFFF else 0
            cmp = cmp>>31;

            // Here is the trick, this eliminates one conditional jump
            low = (low&~cmp) + ((mid + 1)&cmp);
            high = (high&cmp) + ((mid - 1)&~cmp);
        }
        return -(low + 1);  // key not found.
    }


    @Benchmark
    public void optimized(){
        int mid = ArrayBinarySearchTest.mid;
        Object[] vals = ArrayBinarySearchTest.vals;
        int len = vals.length;
        int res = 0;
        for(Integer i=0;i<mid;i++){
            res += binarySearch_optimized(vals, 0, len, i);
        }
        if(res==0)
            System.out.println("aa");
    }


    /** verify that our optimized method is actually faster */
    @Test
    public void verify_optimized(){
        int max = ((Integer)vals[vals.length-1]) + 100;
        for(Integer i=0; i<max;i++){
            int index1 = binarySearch0(vals, 0, vals.length, i);
            int index2 = binarySearch_optimized(vals, 0, vals.length, i);
            assertEquals(index1, index2);
        }
    }

}
