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

package py.common.struct;

public class Pair<A, B> extends Object {
  private A first;
  private B second;

  public Pair() {
    this.first = null;
    this.second = null;
  }

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public A getFirst() {
    return first;
  }

  public void setFirst(A first) {
    this.first = first;
  }

  public B getSecond() {
    return second;
  }

  public void setSecond(B second) {
    this.second = second;
  }

  @Override
  public String toString() {
    return "(" + first + ", " + second + ")";
  }

  private boolean equals(Object x, Object y) {
    return (x == null && y == null) || (x != null && x.equals(y));
  }

  @Override
  public boolean equals(Object other) {
    return
        other instanceof Pair
            && equals(first, ((Pair) other).first)
            && equals(second, ((Pair) other).second);
  }

  @Override
  public int hashCode() {
    if (first == null) {
      return (second == null) ? 0 : second.hashCode() + 1;
    } else if (second == null) {
      return first.hashCode() + 2;
    } else {
      return first.hashCode() * 17 + second.hashCode();
    }
  }
}
