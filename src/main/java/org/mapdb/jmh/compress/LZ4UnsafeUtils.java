package org.mapdb.jmh.compress;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static net.jpountz.util.UnsafeUtils.readByte;
import static net.jpountz.util.UnsafeUtils.readInt;
import static net.jpountz.util.UnsafeUtils.readLong;
import static net.jpountz.util.UnsafeUtils.readShort;
import static net.jpountz.util.UnsafeUtils.writeByte;
import static net.jpountz.util.UnsafeUtils.writeInt;
import static net.jpountz.util.UnsafeUtils.writeLong;
import static net.jpountz.util.UnsafeUtils.writeShort;
import static net.jpountz.util.Utils.NATIVE_BYTE_ORDER;

import java.nio.ByteOrder;

public enum LZ4UnsafeUtils {
  ;

  static void safeArraycopy(byte[] src, int srcOff, byte[] dest, int destOff, int len) {
    final int fastLen = len & 0xFFFFFFF8;
    wildArraycopy(src, srcOff, dest, destOff, fastLen);
    for (int i = 0, slowLen = len & 0x7; i < slowLen; i += 1) {
      writeByte(dest, destOff + fastLen + i, readByte(src, srcOff + fastLen + i));
    }
  }

  static void wildArraycopy(byte[] src, int srcOff, byte[] dest, int destOff, int len) {
    for (int i = 0; i < len; i += 8) {
      writeLong(dest, destOff + i, readLong(src, srcOff + i));
    }
  }


  static void safeIncrementalCopy(byte[] dest, int matchOff, int dOff, int matchLen) {
    for (int i = 0; i < matchLen; ++i) {
      dest[dOff + i] = dest[matchOff + i];
      writeByte(dest, dOff + i, readByte(dest, matchOff + i));
    }
  }

  static int readShortLittleEndian(byte[] src, int srcOff) {
    short s = readShort(src, srcOff);
    if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
      s = Short.reverseBytes(s);
    }
    return s & 0xFFFF;
  }

  static void writeShortLittleEndian(byte[] dest, int destOff, int value) {
    short s = (short) value;
    if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
      s = Short.reverseBytes(s);
    }
    writeShort(dest, destOff, s);
  }

  static boolean readIntEquals(byte[] src, int ref, int sOff) {
    return readInt(src, ref) == readInt(src, sOff);
  }

  static int commonBytes(byte[] src, int ref, int sOff, int srcLimit) {
    int matchLen = 0;
    while (sOff <= srcLimit - 8) {
      if (readLong(src, sOff) == readLong(src, ref)) {
        matchLen += 8;
        ref += 8;
        sOff += 8;
      } else {
        final int zeroBits;
        if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
          zeroBits = Long.numberOfLeadingZeros(readLong(src, sOff) ^ readLong(src, ref));
        } else {
          zeroBits = Long.numberOfTrailingZeros(readLong(src, sOff) ^ readLong(src, ref));
        }
        return matchLen + (zeroBits >>> 3);
      }
    }
    while (sOff < srcLimit && readByte(src, ref++) == readByte(src, sOff++)) {
      ++matchLen;
    }
    return matchLen;
  }

  static int writeLen(int len, byte[] dest, int dOff) {
    while (len >= 0xFF) {
      writeByte(dest, dOff++, 0xFF);
      len -= 0xFF;
    }
    writeByte(dest, dOff++, len);
    return dOff;
  }

  static int commonBytesBackward(byte[] b, int o1, int o2, int l1, int l2) {
    int count = 0;
    while (o1 > l1 && o2 > l2 && readByte(b, --o1) == readByte(b, --o2)) {
      ++count;
    }
    return count;
  }
}

