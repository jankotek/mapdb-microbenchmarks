package org.mapdbd;


import net.jpountz.lz4.LZ4Factory;
import net.jpountz.util.UnsafeUtils;
import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

/** best way to calculate checksum of header, 32KB of data? */
@State(Scope.Thread)
public class HeadChecksum {

    final byte[] data = new byte[64*1024];
    {new Random().nextBytes(data);}

    final CRC32 crc = new CRC32();
    final Adler32 adler = new Adler32();

    @Benchmark public  long crc32(){

        crc.reset();
        crc.update(data,0,data.length);
        return crc.getValue();
    }


    @Benchmark public long adler32(){
        adler.reset();
        adler.update(data, 0, data.length);
        return adler.getValue();
    }

    @Benchmark  public long longUnsafePlus(){
        byte[] data = this.data;
        long res = 0;
        for(int offset = 0; offset<data.length;offset+=8){
            res += UnsafeUtils.readLong(data,offset);
        }
        return res;
    }

    @Benchmark  public long longUnsafePlus2(){
        byte[] data = this.data;
        long res = 0;
        for(int offset = 0; offset<data.length;offset+=8){
            res += offset + UnsafeUtils.readLong(data,offset);
        }
        return res;
    }

    @Benchmark  public long longSafePlus2(){
        byte[] data = this.data;
        long res = 0;
        for(int offset = 0; offset<data.length;offset+=8){
            res += offset + getLong(data, offset);
        }
        return res;
    }

    @Benchmark  public long longSafePlus3(){
        byte[] data = this.data;
        long res = 0;
        for(int offset = 0; offset<data.length;offset+=8){
            res += -7046029254386353131L*(offset + getLong(data, offset));
        }
        return res;
    }


    public static long getLong(byte[] buf, int pos) {
        return
                ((((long)buf[pos++]) << 56) |
                        (((long)buf[pos++] & 0xFF) << 48) |
                        (((long)buf[pos++] & 0xFF) << 40) |
                        (((long)buf[pos++] & 0xFF) << 32) |
                        (((long)buf[pos++] & 0xFF) << 24) |
                        (((long)buf[pos++] & 0xFF) << 16) |
                        (((long)buf[pos++] & 0xFF) <<  8) |
                        (((long)buf[pos] & 0xFF)));

    }


    public static int getInt(byte[] buf, int pos) {
        return
                        (((int)buf[pos++] & 0xFF) << 24) |
                        (((int)buf[pos++] & 0xFF) << 16) |
                        (((int)buf[pos++] & 0xFF) <<  8) |
                        (((int)buf[pos] & 0xFF));
    }



    @Benchmark  public long longUnsafeMultiple(){
        byte[] data = this.data;
        long res = 0;
        for(int offset = 0; offset<data.length;offset+=8){
            res *= 31L*offset* UnsafeUtils.readLong(data,offset);
        }
        return res;
    }


    final XXHash32 xhashSafe = XXHashFactory.safeInstance().hash32();

    final XXHash32 xhashUnSafe = XXHashFactory.unsafeInstance().hash32();

    @Benchmark  public long xhash_safe(){
        return xhashSafe.hash(data,0,data.length,0);
    }


    @Benchmark  public long xhash_unsafe(){
        return xhashUnSafe.hash(data,0,data.length,0);
    }
}
