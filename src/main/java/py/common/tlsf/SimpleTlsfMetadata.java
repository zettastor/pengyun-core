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

package py.common.tlsf;

public class SimpleTlsfMetadata implements TlsfMetadata {
  private long firstLevelBitmap = 0L;

  private long[] secondLevelBitmap = new long[TlsfSpaceManager.FIRST_LEVEL_INDEX_COUNT];

  private long[][] segregatedList = new long[TlsfSpaceManager
      .FIRST_LEVEL_INDEX_COUNT][TlsfSpaceManager.SECOND_LEVEL_INDEX_COUNT];

  public SimpleTlsfMetadata() {
    for (int i = 0; i < TlsfSpaceManager.FIRST_LEVEL_INDEX_COUNT; i++) {
      for (int j = 0; j < TlsfSpaceManager.SECOND_LEVEL_INDEX_COUNT; j++) {
        segregatedList[i][j] = TlsfSpaceManager.NULL_SPACE_ADDRESS;
      }
    }
  }

  @Override
  public long getFirstLevelBitmap() {
    return firstLevelBitmap;
  }

  @Override
  public void setFirstLevelBitmap(long bitmap) {
    this.firstLevelBitmap = bitmap;
  }

  @Override
  public long getSecondLevelBitmap(int firstLevelIndex) {
    return secondLevelBitmap[firstLevelIndex];
  }

  @Override
  public void setSecondLevelBitmap(int firstLevelIndex, long bitmap) {
    secondLevelBitmap[firstLevelIndex] = bitmap;
  }

  @Override
  public long getItemFromSegregatedList(int firstLevelIndex, int secondLevelIndex) {
    return segregatedList[firstLevelIndex][secondLevelIndex];
  }

  @Override
  public void setItemToSegregatedList(int firstLevelIndex, int secondLevelIndex, long address) {
    segregatedList[firstLevelIndex][secondLevelIndex] = address;
  }
}
