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

package py.io.sequential;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.junit.Test;
import py.io.sequential.IoSequentialTypeHolder.IoSequentialType;
import py.test.TestBase;

public class RandomSequentialIdentifierV2ImplTest extends TestBase {
  @Test
  public void testAllSequential() throws Exception {
    int blockSize = 1024;
    int sequentialLimit = 4;
    long offset = 4096;
    RandomSequentialIdentifierV2 sequentialIdentifierV2 = new RandomSequentialIdentifierV2Impl(
        sequentialLimit, "");
    List<IoSequentialTypeHolder> ioContexts = new ArrayList<>();
    int listSize = sequentialLimit + 1;
    for (int i = 0; i < listSize; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);

    Validate.isTrue(ioContexts.size() == listSize);
    for (int i = 0; i < listSize; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }

    ioContexts.clear();
    buildIoContextList(ioContexts, offset, blockSize);
    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);
    Validate.isTrue(ioContexts.get(0).getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
  }

  @Test
  public void testRandom() throws Exception {
    int blockSize = 1024;
    long offset = 4096;
    List<IoSequentialTypeHolder> ioContexts = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    offset += blockSize;
    for (int i = 0; i < 4; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    offset += blockSize;
    buildIoContextList(ioContexts, offset, blockSize);
    int sequentialLimit = 4;
    RandomSequentialIdentifierV2 sequentialIdentifierV2 = new RandomSequentialIdentifierV2Impl(
        sequentialLimit, "");
    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);

    for (int i = 0; i < 7; i++) {
      Validate.isTrue(ioContexts.get(i).getIoSequentialType() == IoSequentialType.RANDOM_TYPE);
    }

    Validate.isTrue(ioContexts.get(7).getIoSequentialType() == IoSequentialType.UNKNOWN);
  }

  @Test
  public void testSequentialRandomAndTailSequential() throws Exception {
    int blockSize = 1024;
    long offset = 1024;

    offset = 1024;
    List<IoSequentialTypeHolder> ioContexts = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    offset = 8 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);
    offset = 9 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);

    offset = 11 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);
    offset = 12 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);

    int sequentialLimit = 3;
    RandomSequentialIdentifierV2 sequentialIdentifierV2 = new RandomSequentialIdentifierV2Impl(
        sequentialLimit, "");
    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);

    for (int i = 0; i < 4; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }

    for (int i = 4; i < 6; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.RANDOM_TYPE);
    }

    for (int i = 6; i < 8; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }
  }

  @Test
  public void testRandomAndSequentialAndRandom() throws Exception {
    int blockSize = 1024;
    long offset = 1024;

    List<IoSequentialTypeHolder> ioContexts = new ArrayList<>();
    buildIoContextList(ioContexts, offset, blockSize);
    offset = 3 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);
    offset = 5 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);
    offset = 7 * 1024;
    for (int i = 0; i < 4; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    offset = 12 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);

    int sequentialLimit = 3;
    RandomSequentialIdentifierV2 sequentialIdentifierV2 = new RandomSequentialIdentifierV2Impl(
        sequentialLimit, "");
    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);

    for (int i = 0; i < 3; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.RANDOM_TYPE);
    }

    for (int i = 3; i < 7; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }

    int listSize = 8;
    Validate.isTrue(ioContexts.get(listSize - 1).getIoSequentialType() == IoSequentialType.UNKNOWN);

  }

  @Test
  public void testReplicateOffset() throws Exception {
    int blockSize = 1024;
    int sequentialLimit = 3;
    long offset = 1024;
    RandomSequentialIdentifierV2 sequentialIdentifierV2 = new RandomSequentialIdentifierV2Impl(
        sequentialLimit, "");
    List<IoSequentialTypeHolder> ioContexts = new ArrayList<>();
    int listSize = 4;
    offset = 100 * 1024;
    for (int i = 0; i < 4; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);
    for (int i = 0; i < 4; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }

    ioContexts.clear();

    offset = 102 * 1024;
    for (int i = 0; i < 3; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    offset = 200 * 1024;
    for (int i = 3; i < 7; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);

    for (int i = 0; i < 3; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }

    for (int i = 3; i < 7; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }
  }

  @Test
  public void testLastOffset() throws Exception {
    int blockSize = 1024;
    int sequentialLimit = 3;
    long offset = 1024;
    RandomSequentialIdentifierV2 sequentialIdentifierV2 = new RandomSequentialIdentifierV2Impl(
        sequentialLimit, "");
    List<IoSequentialTypeHolder> ioContexts = new ArrayList<>();
    int listSize = 4;
    offset = 100 * 1024;
    for (int i = 0; i < 4; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);
    for (int i = 0; i < 4; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }

    ioContexts.clear();
    offset = 1024;
    for (int i = 0; i < 3; i++) {
      buildIoContextList(ioContexts, offset, blockSize);
      offset += blockSize;
    }

    offset = 104 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);

    offset = 105 * 1024;
    buildIoContextList(ioContexts, offset, blockSize);

    sequentialIdentifierV2.judgeIoIsSequential(ioContexts);

    for (int i = 0; i < 3; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.RANDOM_TYPE);
    }

    for (int i = 3; i < 5; i++) {
      IoSequentialTypeHolder ioContext = ioContexts.get(i);
      Validate.isTrue(ioContext.getIoSequentialType() == IoSequentialType.SEQUENTIAL_TYPE);
    }

  }

  private void buildIoContextList(List<IoSequentialTypeHolder> list, long offset, int length) {
    IoSequentialTypeHolder ioContext = new DummyIoContext(offset, length);
    list.add(ioContext);
  }

  class DummyIoContext implements IoSequentialTypeHolder {
    private long offset;
    private int length;
    private IoSequentialType ioSequentialType;

    public DummyIoContext(long offset, int length) {
      this.offset = offset;
      this.length = length;
      ioSequentialType = IoSequentialType.UNKNOWN;
    }

    @Override
    public long getOffset() {
      return offset;
    }

    @Override
    public int getLength() {
      return length;
    }

    @Override
    public IoSequentialType getIoSequentialType() {
      return ioSequentialType;
    }

    @Override
    public void setIoSequentialType(IoSequentialType ioSequentialType) {
      this.ioSequentialType = ioSequentialType;
    }
  }
}
