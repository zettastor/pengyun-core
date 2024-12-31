/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 * Portions Copyrighted 2010 Forgerock
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
