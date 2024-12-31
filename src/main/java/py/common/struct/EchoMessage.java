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
