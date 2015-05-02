package org.mapdb.jmh;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;

@State(Scope.Thread)
public class StringReadWrite {

    byte[] bb = new byte[128];
    String ss = "139012903901fsklvsdklfklscvsd";


    public static int  packInt(byte[] buf, int pos, int value) throws IOException {
        int shift = 31-Integer.numberOfLeadingZeros(value);
        shift -= shift%7; // round down to nearest multiple of 7
        while(shift!=0){
            buf[pos++] = (byte) (((value>>>shift) & 0x7F) | 0x80);
            shift-=7;
        }
        buf[pos++] = (byte) (value & 0x7F);
        return pos;
    }



    public static int  packIntShorter(byte[] buf, int pos, int value) throws IOException {
        ///We can avoid the loop and calling Integer.numberOfLeadingZeros very easily when writing small integers.
        // This helps serialization of ASCII strings quite a bit.
        // see https://github.com/jankotek/MapDB/pull/489
        // credit Max Bolingbroke

        // Optimize for the common case where value is small. This is particular important where our caller
        // is SerializerBase.SER_STRING.serialize because most chars will be ASCII characters and hence in this range.
        int shift = (value & ~0x7F);
        if ( shift!= 0) {
            //$DELAY$
            shift = 31-Integer.numberOfLeadingZeros(value);
            shift -= shift%7; // round down to nearest multiple of 7
            while(shift!=0){
                buf[pos++] = ((byte) (((value>>>shift) & 0x7F) | 0x80));
                //$DELAY$
                shift-=7;
            }
        }
        //$DELAY$
        buf[pos++] = ((byte) (value & 0x7F));
        return pos;
    }
    @Benchmark
    public void writeUTF() throws IOException {
        String s = ss;
        byte[] b = bb;


        final int len = s.length();
        int pos = 1;
        pos=packInt(b,pos,len);
        for (int i = 0; i < len; i++) {
            //$DELAY$
            int c = (int) s.charAt(i);
            pos=packInt(b,pos,c);
        }
    }


    @Benchmark
    public void writeUTFShorter() throws IOException {
        String s = ss;
        byte[] b = bb;


        final int len = s.length();
        int pos = 1;
        pos=packInt(b,pos,len);
        for (int i = 0; i < len; i++) {
            //$DELAY$
            int c = (int) s.charAt(i);
            pos=packIntShorter(b,pos,c);
        }
    }


    @Benchmark
    public void writeUTF_inlined() throws IOException {
        String s = ss;
        byte[] b = bb;


        final int len = s.length();
        int pos = 1;



        for (int i = 0; i < len; i++) {
            //$DELAY$
            char c =  s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                b[pos++] = (byte) c;

            } else if (c > 0x07FF) {
                b[pos++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                b[pos++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                b[pos++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                b[pos++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                b[pos++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
    }


    @Benchmark
    public void writeUTF_inlined2() throws IOException {
        String s = ss;
        byte[] b = bb;

        int pos =1;
        final int len = s.length();

        int v = len;
        int shift;
        int i=-1;

        for(;;) {

            shift = 31 - Integer.numberOfLeadingZeros(v);
            shift -= shift % 7; // round down to nearest multiple of 7
            while (shift != 0) {
                b[pos++] = (byte) (((v >>> shift) & 0x7F) | 0x80);
                shift -= 7;
            }
            b[pos++] = (byte) (v & 0x7F);

            if (++i == len)
                return;
            v = s.charAt(i);
        }
    }



    @Benchmark
    public void writeUTF_dataInput() throws IOException {
        String str = ss;
        byte[] bytearr = bb;

        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }




        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i=0;
        for (i=0; i<strlen; i++) {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F))) break;
            bytearr[count++] = (byte) c;
        }

        for (;i < strlen; i++){
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }

    }


}
