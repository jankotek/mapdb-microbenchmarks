package org.mapdbd;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Arrays;
import java.util.Random;

@State(Scope.Thread)
public class HTreeMapBench {

    byte[] s1 = new byte[128];
    byte[] s2 = new byte[128];
    {
        Arrays.fill(s2, (byte) 0xFF);
    }




    /** converts hash slot into actuall offset in dir array, using bitmap */
    protected static final int dirOffsetFromSlot(byte[] dir, int slot) {

        //traverse bitmap, increment offset for each non zero bit
        int offset = 16;
        for(int i=0;;i++){

            int val = dir[i];
            for(int j=0;j<8;j++){
                //at slot position, return
                if(slot--==0) {
                    return ((val & 1)==0?-1:1) * offset;
                }
                offset += 6*(val & 1);
                val = val>>>1;
            }
        }
    }



    @Benchmark
    public void s1_3(){
        dirOffsetFromSlot(s1,3*8);
    }

    @Benchmark
    public void s2_3(){
        dirOffsetFromSlot(s2,3*8);
    }

    @Benchmark
    public void s1_15(){
        dirOffsetFromSlot(s1,14*8+7);
    }

    @Benchmark
    public void s2_15(){
        dirOffsetFromSlot(s2,14*8+7);
    }

    /** converts hash slot into actuall offset in dir array, using bitmap */
    protected static final int dirOffsetFromSlotX(byte[] dir, int slot) {
        int isSet = ((dir[slot>>3] >>> (slot&7)) & 1); //check if bit at given slot is set
        isSet <<=2; //multiply by two, so it is usable in multiplication

        int offset=0;
        int val = slot>>>3;
        int dirPos=0;
        while(dirPos!=val){
            offset+=Integer.bitCount(dir[dirPos++]&0xFF);
        }

        slot = (1<<(slot&7))-1; //turn slot into mask for N right bits

        val = dir[dirPos] & slot;
        offset += Integer.bitCount(val);

        offset = 16 + offset*6; //normalize offset

        return -offset + isSet*offset; //turn into negative value if bit is not set, do not use conditions

    }


    @Benchmark
    public void sX1_3(){
        dirOffsetFromSlotX(s1, 3 * 8);
    }

    @Benchmark
    public void sX2_3(){
        dirOffsetFromSlotX(s2, 3 * 8);
    }

    @Benchmark
    public void sX1_15(){
        dirOffsetFromSlotX(s1, 14 * 8 + 7);
    }

    @Benchmark
    public void sX2_15(){
        dirOffsetFromSlotX(s2, 14 * 8 + 7);
    }


}
