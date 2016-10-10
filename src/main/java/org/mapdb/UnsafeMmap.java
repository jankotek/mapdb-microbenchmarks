package org.mapdb;


import org.openjdk.jmh.annotations.*;
import sun.misc.Unsafe;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class UnsafeMmap {

    final MappedByteBuffer v;
    final long address;

    static final long size = 24*1024*1024;
    static final Unsafe unsafe = Utils.getUnsafe();

    public UnsafeMmap() {
        try {
            File f = new File("/mnt/test/mapdb"+System.currentTimeMillis());//File.createTempFile("mapdb", "mapdb");
            f.deleteOnExit();
            OutputStream o = new FileOutputStream(f);
            byte[] b = new byte[1024];
            for (long offset = 0; offset < size; offset += b.length) {
                o.write(b);
            }
            o.flush();
            o.close();
            v = new RandomAccessFile(f, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, size);
            address = ((sun.nio.ch.DirectBuffer) v).address();
        }catch(IOException e){
            throw new IOError(e);
        }
    }


    @Benchmark
    public long buffer_long(){
        long ret = 0;
        for(int offset = 0;offset<size;offset+=8){
            ret+=v.getLong(offset);
        }
        return ret;
    }


    @Benchmark
    public long buffer_byte(){
        long ret = 0;
        for(int offset = 0;offset<size;offset++){
            ret+=v.get(offset);
        }
        return ret;
    }


    @Benchmark
    public long unsafe(){
        long ret = 0;
        for(int offset = 0;offset<size;offset+=8){
            ret+= unsafe.getLong(address+offset);
        }
        return ret;
    }


    @Benchmark
    public long entire(){
        byte[] all = new byte[(int) size];
        ByteBuffer v2 = v.duplicate();
        v2.position(0);
        v2.get(all);
        return all[0];
    }
}
