package org.mapdb.hash;

import org.openjdk.jmh.annotations.Benchmark;

public class Tuple4Hash {


    public static final class Tuple4 {
        private final int c1, c2, c3, c4;

        public Tuple4(int c1, int c2, int c3, int c4) {
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
            this.c4 = c4;
        }

        public int getC1() {
            return c1;
        }

        public int getC2() {
            return c2;
        }

        public int getC3() {
            return c3;
        }

        public int getC4() {
            return c4;
        }


        public int hash31() {
            int h = c1;
            h = h * 31 + c2;
            h = h * 31 + c3;
            h = h * 31 + c4;
            return h;
        }

        public int hashMid() {
            int h = c1;
            h = h * 92821 + c2;
            h = h * 92821 + c3;
            h = h * 92821 + c4;
            return h;
        }


        public int hashLarge() {
            int h = c1;
            h = h * 1322837333 + c2;
            h = h * 1322837333 + c3;
            h = h * 1322837333 + c4;
            return h;
        }
    }
    @Benchmark public void plus31(){
        new Tuple4(1,2,3,4).hash31();
    }


    @Benchmark public void plusMid(){
        new Tuple4(1,2,3,4).hashMid();
    }


    @Benchmark public void plusLarge(){
        new Tuple4(1,2,3,4).hashLarge();
    }

}