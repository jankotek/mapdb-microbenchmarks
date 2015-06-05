package org.mapdb.jmh.compress;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;


/**
 *  Hello world application to demonstrate storage open, commit and close operations
 */
@State(Scope.Thread)
@Warmup(iterations = 0)
@Measurement(iterations = 2,time = 10, timeUnit = TimeUnit.SECONDS)

public class Bench {

    static String s = "Appendix: Storage formats\n" +
            "============================\n" +
            "\n" +
            "\n" +
            "Parity\n" +
            "---------\n" +
            "\n" +
            "MapDB uses parity bits to check storage pointers for corruption.\n" +
            "Offsets are usually aligned to multiples of 8, so lower bits are used for parity checks and other flags.\n" +
            "\n" +
            "One important requirement is that zero value is not valid parity value.\n" +
            "\n" +
            "Parity 1\n" +
            "~~~~~~~~~~~~\n" +
            "Used for multiples of two, lowest bit stores parity information. It is calculated as number of bits set,\n" +
            "lowest bit is than set so total number of non-zero bits is odd:\n" +
            "\n" +
            ".. code:: java\n" +
            "\n" +
            "    val | ((Long.bitCount(val)+1)%2)\n" +
            "\n" +
            "\n" +
            "Methods: ``DataIO.parity1set()`` and ``DataIO.parity1get()``\n" +
            "\n" +
            "Feature bitmap header\n" +
            "----------------------\n" +
            "Feature bitmap is 64bits stored in header. It indicates features storage was created with.\n" +
            "Some of those affect storage format (compression, checksums) and must be enabled to make store readable. \n" +
            "Some slots are not yet used and are reserved for future features. If such unknown bit is set, \n" +
            "MapDB might refuse to open storage, with exception that never version should be used\n" +
            "\n" +
            "Currently used feature bits are:\n" +
            "\n" +
            "1) LZW record compresson enabled\n" +
            "\n" +
            "2) XTEA record encryption enabled. User must supply password to open database.\n" +
            "\n" +
            "3) CRC32 record checksum enabled\n" +
            "\n" +
            "4) Store does not track free space. There might be unclaimed free space between records, this makes free space metrics invalid.\n" +
            "\n" +
            "5) Sharded engine. It means that name catalog or class catalog might not be present\n" +
            "\n" +
            "6) Created from backup. Storage was not created empty, but from existing database, as backup or from data pump\n" +
            "\n" +
            "7) External index table. Index table is not stored in this Volume (file), but somewhere outside, most likely in external file\n" +
            "\n" +
            "8) Compaction disabled. Some features might prevent compaction, for example StoreAppend in single file. \n" +
            "\n" +
            "9) Paranoid. Store was created by patched MapDB it added extra information to catch bugs and data corrupton. \n" +
            "This store can not be opened with normal mapdb.\n" +
            "  \n" +
            "10) Disable parity bit checks. Stor was created by patched MapDB. For extra performance some checksums were disabled.\n" +
            "This store can not be opened with normal mapdb.\n" +
            "\n" +
            "11) Block encryption enabled\n" +
            "   \n" +
            "\n" +
            "StoreDirect\n" +
            "------------------\n" +
            "\n" +
            "StoreDirect uses update in place. It keeps track of space freed by record deletion and reuses it.\n" +
            "It has zero protection from crash, all updates are written directly into store.\n" +
            "It has fast writes, but data corruption is almost guaranteed in case JVM crashes during write.\n" +
            "StoreDirect uses checksums as passive protection to return incorrect data after corruption.\n" +
            "\n" +
            "StoreDirect allocates space in 'pages' of size 1MB. Operations such as ``readLong``, ``readByte[]``\n" +
            "must be aligned so they do not cross page boundaries.\n" +
            "\n" +
            "Head\n" +
            "~~~~~~~\n" +
            "Header is composed by number of 8 byte longs:\n" +
            "\n" +
            "\n" +
            "0) **header** and **head checksum**. Checksum is CRC of entire HEAD and is recalculated on\n" +
            "every sync/close. Invalid checksum means that store was not closed correctly,\n" +
            "is very likely corrupted and MapDB should fail to open it. See ``StoreDirect.headChecksum()``\n" +
            "\n" +
            "1) bit field indicating **format features**. IE what type of checksums are enabled, compression enabled etc...\n" +
            "\n" +
            "2) **store size** pointer to last allocated page inside store. Parity 16.\n" +
            "\n" +
            "3) **max recid** maximal allocated recid. Shifted <<<3 for parity 3\n" +
            "\n" +
            "4) **index page registry** points to page with list of index pages. Parity 16.\n" +
            "\n" +
            "5) **free recids** longs stack\n" +
            "\n" +
            "TODO free longstacks\n" +
            "\n" +
            "TODO rest of zero page is filled by recids\n" +
            "\n" +
            "\n" +
            "\n" +
            "Index page\n" +
            "~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "Linked list of pages. It starts at **index page registry**.\n" +
            "\n" +
            "Composed of series of 8 byte values, most of page repeats:\n" +
            "\n" +
            "- index value\n" +
            "\n" +
            "Start of page:\n" +
            "\n" +
            "- first value is **pointer to next index page**, Parity 16\n" +
            "- second value in page is **checksum of all values** on page (add all values)\n" +
            "\n" +
            "Index Value\n" +
            "~~~~~~~~~~~~~\n" +
            "Index value translates Record ID (recid) into offset in file and record size. Position and size of record might\n" +
            "change as data are updated, that makes index tables necessary. Index Value is 8 byte long with parity 1\n" +
            "\n" +
            "- **bite 49-64** - 16 bite record size. Use ``val>>48`` to get it\n" +
            "\n" +
            "- **bite 5-48** - 48 bite offset, records are aligned to 16 bytes, so last four bites can be used for something else.\n" +
            "  Use ``val&MOFFSET`` to get it\n" +
            "\n" +
            "- **bite 4** - linked or null, indicates if record is linked (see section TODO link to section). Also ``linked && size==0`` indicates null record. Use ``val&MLINKED``.\n" +
            "\n" +
            "- **bite 3** - indicates unused (preallocated or deleted) record. This record is destroyed by compaction. Use ``val&MUNUSED``\n" +
            "\n" +
            "- **bite 2** - archive flag. Set by every modification, cleared by incremental backup. Use ``val&MARCHIVE``\n" +
            "\n" +
            "- **bite 1** - parity bit\n" +
            "\n" +
            "Linked records\n" +
            "~~~~~~~~~~~~~~~~~\n" +
            "Maximal record size is 64KB (16bits). To store larger records StoreDirect links multiple records into single one.\n" +
            "Linked records starts with Index Value where Linked Record is indicates by a bit. If this bit is not set, entire record\n" +
            "is reserved for record data. If Linked bit is set, than first 8 bytes store offset and size to link to next parts.\n" +
            "\n" +
            "Structure of Record Link is similar to Index Value. Except parity is 3.\n" +
            "\n" +
            "- **bite 49-64** - 16 bite record size of next link. Use ``val>>48`` to get it\n" +
            "- **bite 5-48** - 48 bite offset of next record alligned to 16 bytes. Use ``val&MOFFSET`` to get it\n" +
            "- **bite 4** - true if next record is linked, false if next record is last and not linked (is tail of linked record). Use ``val&MLINKED``\n" +
            "- **bite 1-3** - parity bits\n" +
            "\n" +
            "Tail of linked record (last part) does not have 8-byte Record Link at beginning.\n" +
            "\n" +
            "\n" +
            "Long Stack\n" +
            "~~~~~~~~~~~~\n" +
            "Long Stack is linked queue of longs stored as part of storage. It supports two operations: put and take, longs are\n" +
            "returned in FIFO order. StoreDirect uses this structure to keep track of free space. Space allocation involves\n" +
            "taking long from stack. There are more stack, one to keep track of free recids. For space usage there are in total\n" +
            "``64K / 16 = 4096`` Long Stacks (maximal non-linked record size is 64K and records are aligned to 16 bytes).\n" +
            "\n" +
            "Long stack is organized similar way as linked record. It is stored in chunks, each chunks contains multiple long\n" +
            "values and link to next chunk. Chunks size varies. Long values are stored in bidirectional-packed form, to make\n" +
            "unpacking possible in both directions.  Single value occupies from 2 bytes to 9 bytes. TODO explain bidi-packing.\n" +
            "\n" +
            "Each Long Stack is identified by master pointer, which points to its last chunk. Master Pointer for each Long Stack\n" +
            "is stored in head of storage file at its reserved offset. Head chunk is not linked directly, one has to fully\n" +
            "traverse Long Stack to get to head.\n" +
            "\n" +
            "Structure of Long Stack Chunk is as follow:\n" +
            "\n" +
            "- **byte 1-4** optional checksum of this chunk\n" +
            "- **byte 5-6** total size of this chunk.\n" +
            "- **byte 7-12** pointer to previous chunk in this long stack. Parity 4, parity is shared with total size at byte 5-6.\n" +
            "- rest of chunk is filled with bidi-packed longs with parity 1\n" +
            "\n" +
            "Master Link structure:\n" +
            "\n" +
            " - **byte 1-2** tail pointer, points where long values are ending at current chunk. Its value changes on every take/put.\n" +
            " - **byte 3-8** chunk offset, parity 4.\n" +
            "\n" +
            "Adding value to Long Stack goes as follow:\n" +
            "\n" +
            "1) check if there is space in current chunk, if not allocate new one and update master pointer\n" +
            "2) write packed value at end of current chunk\n" +
            "3) update tail pointer in Master Link\n" +
            "\n" +
            "Taking value:\n" +
            "\n" +
            "1) check if stack is not empty, return zero if true\n" +
            "2) read value from tail and zero out its bits\n" +
            "3) update tail pointer in Master Link\n" +
            "4) if tail pointer is 0 (empty), delete current chunk and update master pointer to previous page\n" +
            "\n" +
            "\n" +
            "Write Ahead Log\n" +
            "-------------------------\n" +
            "\n" +
            "WAL protects storage from data corruption if transactions are enabled. Technically it is sequence of instructions written to append-only file. Each\n" +
            "instruction says something like: 'write this data at this offset'. TODO explain WAL.\n" +
            "\n" +
            "WAL is stored in sequence of files.\n" +
            "\n" +
            "WAL lifecycle\n" +
            "~~~~~~~~~~~~~~~~~\n" +
            "- open (or create) WAL\n" +
            "- replay if unwritten data exists (described in separate section)\n" +
            "- start new file\n" +
            "- write instruction as they come\n" +
            "- on commit start new file\n" +
            "- sync old file, once sync is done exit commit\n" +
            "- once log is full replay all files\n" +
            "- discard logs and start over\n" +
            "\n" +
            "WAL file format\n" +
            "~~~~~~~~~~~~~~~~~~~\n" +
            "- **byte 1-4** header and file number\n" +
            "- **byte 5-8** CRC32 checksum of entire log file.  TODO perhaps Adler32?\n" +
            "- **byte 9-16** Log Seal, written as last data just before sync.\n" +
            "- rest of file are instructions\n" +
            "- **end of file** - End Of File instruction\n" +
            "\n" +
            "WAL Instructions\n" +
            "~~~~~~~~~~~~~~~~~~\n" +
            "Each instruction starts with single byte header. First 3 bits indicate type of instruction. Last 5 bits contain\n" +
            "checksum to verify instruction.\n" +
            "\n" +
            "Type of instructions:\n" +
            "\n" +
            "0) **end of file**. Last instruction of file. Checksum is ``bit parity from offset & 31``\n" +
            "1) **write long**. Is followed by 8 bytes value and 6 byte offset. Checksum is ``(bit parity from 15 bytes + 1)&31``\n" +
            "2) **write byte[]**. Is followed by 2 bytes size, 6 byte offset and data itself. Checksum is ``(bit parity from 9 bytes + 1 + sum(byte[]))&31``\n" +
            "3) **skip N bytes**. Is followed by 3 bytes value, number of bytes to skip . Used so data do not overlap page size. Checksum is ``(bit parity from 3 bytes + 1)&31``\n" +
            "4) **skip single byte**. Skip single byte in WAL. Checksum is ``bit parity from offset & 31``\n" +
            "5) **record**. Is followed by 6 bytes recid, than 4 bytes record size and an record data. Is used in Record format. Size==-2 is tombstone, size==-1 is null record\n" +
            "    TODO checksum for record inst\n" +
            "\n" +
            "\n" +
            "Append Only Store\n" +
            "--------------------\n" +
            "StoreAppend implements Append-Only log files storage. It is sequence of instructions such as 'update record', 'delete record'\n" +
            "and so on. Optionally store can be split between multiple files, to support online compaction.\n" +
            "\n" +
            "Instructions\n" +
            "~~~~~~~~~~~~~\n" +
            "\n" +
            "1) record update. Followed by recid, size and binary data\n" +
            "2) delete record. Places tombstone in index table. Followed by recid.\n" +
            "3) record insert. Followed by recid, size and binary data\n" +
            "4) preallocate record. Followed by recid\n" +
            "5) skip N bytes. Followed by number of bytes to skip.\n" +
            "6) skip single byte\n" +
            "7) EOF current file. Move to next file\n" +
            "8) Current transaction is valid. Start new transaction\n" +
            "9) Current transaction is invalid. Rollback all changes since end of previous transaction. Start new transaction\n";



        CompressLZF lzf = new CompressLZF();
        byte[] source = s.getBytes();
        byte[] target = new byte[source.length+1000];
        int len = lzf.compress(source, source.length, target, 0);

        @Benchmark
        public void lzf(){
                lzf.expand(target,0,source,0,source.length);
                if(target[0]==-110)
                        throw new InternalError();
        }

        @Benchmark
        public void lzfUnsafe(){
                lzf.expandUnsafe(target, 0, source, 0, source.length);
                if(target[0]==-110)
                        throw new InternalError();
        }

        byte[] data = s.getBytes();
        final int decompressedLength = data.length;

        LZ4Factory factory = LZ4Factory.unsafeInstance();
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
        byte[] restored = new byte[decompressedLength];
        LZ4FastDecompressor decompressor = factory.fastDecompressor();

        @Benchmark
        public int lz4(){
                    return decompressor.decompress(compressed, 0, restored, 0, decompressedLength);
            }

        @Benchmark
        public int lz4_new_array(){
                byte[] restored = new byte[decompressedLength];
                return decompressor.decompress(compressed, 0, restored, 0, decompressedLength);
        }


}