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
