package org.mapdb;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;

@State(Scope.Thread)
//@Warmup(iterations = 2)
//@Measurement(iterations = 5)
public class BTreeNode {

    DataIO.DataInputByteArray in;
    BTreeMap.NodeSerializer ser = new BTreeMap.NodeSerializer(false,BTreeKeySerializer.LONG,Serializer.STRING,0);

    public BTreeNode()  {
        Object[] ll = new Object[16];
        for(int l=0;l<ll.length;l++){
            ll[l] = 1L*l;
        }

        BTreeMap.DirNode n = new BTreeMap.DirNode(BTreeKeySerializer.LONG.arrayToKeys(ll), false,false,false,new int[ll.length]);
        DataIO.DataOutputByteArray out = new DataIO.DataOutputByteArray();
        try {
            ser.serialize(out,n);
        } catch (IOException e) {
            e.printStackTrace();
        }
        in = new DataIO.DataInputByteArray(out.buf);

    }


    @Benchmark public int deser() throws IOException {
        ser.deserialize(in,0);
        in.pos=0;
        return 0;
    }

}
