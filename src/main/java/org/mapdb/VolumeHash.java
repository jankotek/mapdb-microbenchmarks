package org.mapdb;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class VolumeHash {

    final byte[] data;
    final Volume volume;


    public VolumeHash() {
        data = new byte[100];
        new Random().nextBytes(data);
        volume = new Volume.SingleByteArrayVol(data.length);
        volume.putData(0,data,0,data.length);
    }

    @Benchmark public long xxHash_volume(){
        return volume.hash(0,data.length,0);
    }
    @Benchmark public long xxHash_byte(){
        return DataIO.hash(data, 0, data.length, 0);
    }

    @Benchmark public long xxHash_unsafe(){
        return UnsafeStuff.hash(data,0,data.length,0);
    }


    @Benchmark public long crc32(){
        CRC32 c = new CRC32();
        c.update(data,0,data.length);
        return c.getValue();
    }

    @Benchmark public long adler32(){
        Adler32 c = new Adler32();
        c.update(data,0,data.length);
        return c.getValue();
    }

}
