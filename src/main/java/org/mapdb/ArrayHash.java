package org.mapdb;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.Random;

@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class ArrayHash {

    static final byte[] b = new byte[16];
    static{
        new Random().nextBytes(b);
    }


    @Benchmark public int arrays(){
        return Arrays.hashCode(b);
    }

    @Benchmark public int arrays2(){
        int result = 1;
        for (byte element : b)
            result = 31 * result + element;

        return result;
    }

    @Benchmark public int arrays257(){
        int result = 1;
        for (byte element : b)
            result = 257 * result + element;

        return result;
    }

    @Benchmark public int arraysB(){
        int result = 1;
        for (byte element : b)
            result = -1640531527 * result + element;

        return result;
    }



    @Benchmark public int arraysXXX(){
        return XXHash32JavaUnsafe.hash(b,0,b.length,0);
    }


    @Benchmark public int arraysXXXSafe(){
        return XXHash32JavaSafe.hash(b,0,b.length,0);
    }
}
