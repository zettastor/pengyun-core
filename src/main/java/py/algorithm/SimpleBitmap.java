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
