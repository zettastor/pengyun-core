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

package py.transfer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.channel.Channel;
import java.util.concurrent.atomic.AtomicInteger;

public class PyChannelIdManager {
  private BiMap<Integer, Channel> biMapIdChannel;
  private AtomicInteger atomic;

  public PyChannelIdManager(int size) {
    int initialCapacity = 128;
    if (size > 0 && size < 2048) {
      initialCapacity = size;
    }

    biMapIdChannel = HashBiMap.create(initialCapacity);
    atomic = new AtomicInteger(0);
  }

  public BiMap<Integer, Channel> getMapIdChannel() {
    return biMapIdChannel;
  }

  public Channel getChannel(int channelid) {
    return biMapIdChannel.get(channelid);
  }

  public Integer getId(Channel channel) {
    return biMapIdChannel.inverse().get(channel);
  }

  public synchronized void addChannel(Channel channel) {
    if (biMapIdChannel.inverse().get(channel) != null) {
      return;
    }

    biMapIdChannel.put(atomic.getAndIncrement(), channel);
  }

  public synchronized Integer delChannel(Channel channel) {
    return biMapIdChannel.inverse().remove(channel);
  }

  synchronized Channel delChannel(Integer id) {
    return biMapIdChannel.remove(id);
  }
}
