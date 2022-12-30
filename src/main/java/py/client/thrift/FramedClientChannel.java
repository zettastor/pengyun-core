/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package py.client.thrift;

import javax.annotation.concurrent.NotThreadSafe;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.util.Timer;
import py.common.rpc.share.TransDuplexProtocolFactory;

@NotThreadSafe
public class FramedClientChannel extends PyAbstractClientChannel {
  protected FramedClientChannel(Channel channel, Timer timer,
      TransDuplexProtocolFactory protocolFactory,
      int maxChannelPendingSizeMb, int maxPendingRequestCount) {
    super(channel, timer, protocolFactory, maxPendingRequestCount,
        maxChannelPendingSizeMb * 1024 * 1024);
  }

  @Override
  protected ChannelBuffer extractResponse(Object message) {
    if (!(message instanceof ChannelBuffer)) {
      return null;
    }

    ChannelBuffer buffer = (ChannelBuffer) message;
    if (!buffer.readable()) {
      return null;
    }

    return buffer;
  }

  @Override
  protected ChannelFuture writeRequest(ChannelBuffer request) {
    return getNettyChannel().write(request);
  }

}
