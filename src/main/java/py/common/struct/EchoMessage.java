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

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EchoMessage {
  private ByteBuffer bytebuffer;
  private int lenData;
  private Lock lock = new ReentrantLock();
  private Condition condition = lock.newCondition();

  public EchoMessage(ByteBuffer bytebuffer) {
    this.bytebuffer = bytebuffer;
    this.lenData = 0;
  }

  public int getDatalen() {
    return lenData;
  }

  public void setDatalen(int length) {
    lenData = length;
  }

  public ByteBuffer getBuffer() {
    return bytebuffer;
  }

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }

  public void await(int delay) {
    try {
      condition.await(delay, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void signal() {
    condition.signal();
  }
}
