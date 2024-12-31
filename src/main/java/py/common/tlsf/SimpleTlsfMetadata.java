/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
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
