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

package py.algorithm;

import py.common.bitmap.Bitmap;

public class SimpleBitmap implements SimpleBitmapIface {
  private final int size;
  private final Bitmap bitmap;

  public SimpleBitmap(int size) {
    this.size = size;

    size = (size + Byte.SIZE - 1) / Byte.SIZE * Byte.SIZE;
    this.bitmap = new Bitmap(size);
  }

  @Override
  public void set(int index) {
    rangeCheck(index);
    bitmap.set(index);
  }

  @Override
  public boolean get(int index) {
    rangeCheck(index);
    return bitmap.get(index);
  }

  @Override
  public void clear(int index) {
    rangeCheck(index);
    bitmap.clear(index);
  }

  @Override
  public void inverse() {
    bitmap.inverse();
  }

  @Override
  public int size() {
    return size;
  }

  private void rangeCheck(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("index:" + index + " size:" + size);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimpleBitmap)) {
      return false;
    }

    SimpleBitmap that = (SimpleBitmap) o;

    if (size != that.size) {
      return false;
    }
    return bitmap != null ? bitmap.equals(that.bitmap) : that.bitmap == null;
  }

  @Override
  public int hashCode() {
    int result = size;
    result = 31 * result + (bitmap != null ? bitmap.hashCode() : 0);
    return result;
  }
}
