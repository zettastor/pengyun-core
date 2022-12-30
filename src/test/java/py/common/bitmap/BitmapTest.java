/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

package py.common.bitmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import org.apache.commons.lang.Validate;
import org.junit.Test;
import py.test.TestBase;

public class BitmapTest extends TestBase {
  @Test
  public void base() throws Exception {
    BitSet bitSet = new BitSet();
    logger.warn("value: {}", bitSet.get(1));
    bitSet.set(1, true);
    logger.warn("value: {}", bitSet.get(1));
    bitSet.set(1, false);
    logger.warn("value: {}", bitSet.get(1));
    Validate.isTrue(!bitSet.get(1));
  }

  @Test
  public void testBitmapGetNextClearPosition() {
    Bitmap bitmapWith8 = new Bitmap(8);
    for (int i = 0; i < 8; i++) {
      assertEquals(i, bitmapWith8.nextClearBit(i));
      bitmapWith8.set(i);
      assertEquals(i + 1, bitmapWith8.nextClearBit(i));
    }

    logger.debug("bitmapWith8 {}", bitmapWith8);

    try {
      bitmapWith8.nextClearBit(8);
      assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
      logger.error("caught exception", e);
    }

    Bitmap bitmapWith32 = new Bitmap(32);
    for (int i = 0; i < 32; i++) {
      assertEquals(i, bitmapWith32.nextClearBit(i));
      bitmapWith32.set(i);
      assertEquals(i + 1, bitmapWith32.nextClearBit(i));
      assertEquals(i + 1, bitmapWith32.nextClearBit(0));
    }

    Bitmap bitmapWith128 = new Bitmap(128);
    for (int i = 0; i < 128; i++) {
      assertEquals(i, bitmapWith128.nextClearBit(i));
      bitmapWith128.set(i);
      assertEquals(i + 1, bitmapWith128.nextClearBit(i));
    }
  }

  @Test
  public void testBitmapSetAndGetValue() {
    Random r = new Random(System.currentTimeMillis());
    int times = 100;
    for (int i = 1; i <= times; i++) {
      int nbits = i * 8;
      logger.debug("nbits is {}", nbits);
      Bitmap bitmap = new Bitmap(nbits);
     
      HashMap<Integer, Boolean> sample = new HashMap<>();
      for (int j = 0; j < nbits; j++) {
        if (r.nextBoolean()) {
          bitmap.set(j);
          logger.debug("set {} with true", j);
          sample.put(j, Boolean.TRUE);
        } else {
          sample.put(j, Boolean.FALSE);
        }
      }

      logger.debug("bitmap is {}", bitmap);
      for (Entry<Integer, Boolean> entry : sample.entrySet()) {
        assertEquals(entry.getValue().booleanValue(), bitmap.get(entry.getKey()));
        if (entry.getValue().booleanValue()) {
          bitmap.clear(entry.getKey());
        }
      }

      for (Entry<Integer, Boolean> entry : sample.entrySet()) {
        assertTrue(false == bitmap.get(entry.getKey()));
      }

      try {
        bitmap.get(nbits);
        assertTrue(false);
      } catch (IndexOutOfBoundsException e) {
        logger.error("caught exception", e);
      }
    }
  }

  @Test
  public void testGetNextClearPositionFromFirstPosition() {
    int nbits = 8 * 60;
    Random r = new Random(System.currentTimeMillis());
    Bitmap map = new Bitmap(nbits);
    ArrayList<Integer> setPos = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      map.set(i);
      setPos.add(i);
    }

    int cnt = 0;
    while (cnt < nbits / 2) {
      int number = r.nextInt(nbits);
      if (!setPos.contains(number)) {
        map.set(number);
        setPos.add(number);
        cnt++;
        logger.debug("cnt is {}, number is {}", cnt, number);
      }

      int nextPositionInArray = nextClearPositionFromList(setPos, nbits);
      int nextPositionInMap = map.nextClearBit(0);
      logger.debug("nextPositionInArray {}, nextPositionInMap {}", nextPositionInArray,
          nextPositionInMap);
      assertEquals(nextPositionInArray, nextPositionInMap);

    }
  }

  @Test
  public void testNextClearBit() {
    Bitmap bitmap = new Bitmap(8);
    for (int i = 0; i < 8; i++) {
      bitmap.set(i);
    }
    assertEquals(8, bitmap.nextClearBit(0));
  }

  private int nextClearPositionFromList(List<Integer> array, int nbits) {
    for (int i = 0; i < nbits; i++) {
      if (!array.contains(i)) {
        return i;
      }
    }
    return nbits;
  }

  @Test
  public void testTransferToOrFromByteArray() {
    Random r = new Random(System.currentTimeMillis());
    int times = 100;
    for (int i = 1; i <= times; i++) {
      int nbits = i * 8;
      logger.debug("nbits is {}", nbits);
      Bitmap bitmap = new Bitmap(nbits);
     
      HashMap<Integer, Boolean> sample = new HashMap<>();
      for (int j = 0; j < nbits; j++) {
        if (r.nextBoolean()) {
          bitmap.set(j);
          logger.debug("set {} with true", j);
          sample.put(j, Boolean.TRUE);
        } else {
          sample.put(j, Boolean.FALSE);
        }
      }

      byte[] bytes = bitmap.toByteArray();
      Bitmap recoveredBitmap = Bitmap.valueOf(bytes);

      logger.debug("original bitmap {}, recovered bitmap {}", bitmap, recoveredBitmap);

      for (Entry<Integer, Boolean> entry : sample.entrySet()) {
        assertEquals(entry.getValue().booleanValue(), recoveredBitmap.get(entry.getKey()));
      }
    }

  }

  @Test
  public void testBitMapCardinalityWithOneByte() {
    Bitmap bitmap = new Bitmap(8);
    for (int i = 0; i < 8; i++) {
      bitmap.set(i);
      byte[] bytes = bitmap.toByteArray();
     
      assertEquals(i + 1, bitmap.cardinality());
    }
    bitmap.clear(7);
    assertEquals(7, bitmap.cardinality());

    bitmap.set(7);
    for (int i = 0; i < 8; i++) {
      bitmap.clear(i);
      assertEquals(8 - (i + 1), bitmap.cardinality());
    }
  }

  @Test
  public void testBitMapCardinalityWithOneLong() {
    Bitmap bitmap = new Bitmap(64);
    bitmap.set(63);
    assertEquals(1, bitmap.cardinality());

    bitmap.set(1);
    assertEquals(2, bitmap.cardinality());
    bitmap.clear(1);
    bitmap.clear(63);
    assertEquals(0, bitmap.cardinality());

    for (int i = 0; i < 64; i++) {
      bitmap.set(i);
      assertEquals(i + 1, bitmap.cardinality());
    }

    bitmap.clear();
    assertEquals(0, bitmap.cardinality());
  }

  @Test
  public void testBitmapCardinalityWithDifferentLength() {
    int testTimes = 100;
    for (int i = 1; i <= testTimes; i++) {
      int nbits = 8 * i;
      Bitmap bitmap = new Bitmap(nbits);

      logger.debug("bitmap nbits {}", nbits);
      for (int j = 0; j < nbits; j++) {
        bitmap.set(j);
        assertEquals(j + 1, bitmap.cardinality());
      }

      for (int j = 0; j < nbits; j++) {
        bitmap.clear(j);
        assertEquals(nbits - (j + 1), bitmap.cardinality());
      }

      for (int j = 0; j < nbits; j++) {
        bitmap.set(j);
        assertEquals(j + 1, bitmap.cardinality());
      }

      for (int j = nbits - 1; j > 0; j--) {
        bitmap.clear(j);
        assertEquals(j, bitmap.cardinality());
      }
    }
  }

  @Test
  public void testBitmapInverse() {
    Bitmap bitmap1 = new Bitmap(64);
    Bitmap bitmap2 = new Bitmap(64);
    Random random = new Random(System.currentTimeMillis());
    for (int i = 0; i < 64; i++) {
      if (random.nextBoolean()) {
        bitmap1.set(i);
        bitmap2.set(i);
      }
    }

    for (int i = 0; i < 64; i++) {
      if (bitmap1.get(i)) {
        assertTrue(bitmap2.get(i));
      } else {
        assertFalse(bitmap2.get(i));
      }
    }

    bitmap1.inverse();

    for (int i = 0; i < 64; i++) {
      if (bitmap1.get(i)) {
        assertFalse(bitmap2.get(i));
      } else {
        assertTrue(bitmap2.get(i));
      }
    }
  }

}
