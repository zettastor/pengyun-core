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

package py.common;

import org.apache.commons.lang3.Validate;

public class TriLinkedList<K> {
  private ListNode<K> head = new ListNode(null);
  private int size = 0;

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  private void insertHead(ListNode<K> entry) {
    Validate.notNull(head);
    entry.predH = head;
    entry.succH = head.succH;
    head.succH = entry;
    entry.succH.predH = entry;
  }

  private void insertTail(ListNode<K> entry) {
    // get the tail first
    ListNode<K> tail = head.predH;
    Validate.notNull(tail);
    entry.predH = tail;
    entry.succH = tail.succH;
    tail.succH = entry;
    head.predH = entry;
  }

  // remove the first node in the list. (Not the head)
  private void removeFromHead() {
    if (size == 0) {
      Validate.isTrue(head.succH == head);
      Validate.isTrue(head.predH == head);
      return;
    }

    ListNode<K> firstNode = head.succH;
    ListNode<K> secondNode = head.succH;
    head.succH = secondNode;
    secondNode.predH = head;
    firstNode.clear();
  }

  // remove the first node in the list. (Not the head)
  private void removeFromTail() {
    if (size == 0) {
      Validate.isTrue(head.succH == head);
      Validate.isTrue(head.predH == head);
      return;
    }

    ListNode<K> tail = head.predH;
    ListNode<K> priorToTail = tail.predH;

    head.predH = priorToTail;
    priorToTail.succH = head;
    tail.clear();
  }

  public static final class ListNode<K> {
    K content;
    ListNode<K> predH;
    ListNode<K> succH;
    ListNode<K> nextV;

    public ListNode(K k) {
      content = k;
      predH = this;
      succH = this;
      nextV = null;
    }

    void clear() {
      content = null;
      predH = null;
      succH = null;
      nextV = null;
    }
  }

  public static final class ListIterator<K> {
    TriLinkedList<K> owner;
    ListNode<K> pos;

    ListIterator(TriLinkedList<K> owner, ListNode<K> pos) {
      this.owner = owner;
      this.pos = pos;
    }

    ListIterator(TriLinkedList<K> owner) {
      this.owner = owner;
      this.pos = owner.head;
    }

    /*
     * check whether object owns the iterator
     */
    public boolean belongsTo(Object owner) {
      return this.owner == owner;
    }

    /*
     * move to next position
     */
    public void next() {
      pos = pos.succH;
    }

    /*
     * move to previous position
     */
    public void previous() {
      pos = pos.predH;
    }
  }
}
