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

public interface TlsfMetadata {
  public long getFirstLevelBitmap();

  public void setFirstLevelBitmap(long bitmap);

  public long getSecondLevelBitmap(int firstLevelIndex);

  public void setSecondLevelBitmap(int firstLevelIndex, long bitmap);

  public long getItemFromSegregatedList(int firstLevelIndex, int secondLevelIndex);

  public void setItemToSegregatedList(int firstLevelIndex, int secondLevelIndex, long address);
}
