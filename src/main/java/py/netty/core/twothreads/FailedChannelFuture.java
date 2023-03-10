/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package py.netty.core.twothreads;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.PlatformDependent;

final class FailedChannelFuture extends CompleteChannelFuture {
  private final Throwable cause;

  FailedChannelFuture(Channel channel, EventExecutor executor, Throwable cause) {
    super(channel, executor);
    if (cause == null) {
      throw new NullPointerException("cause");
    }
    this.cause = cause;
  }

  @Override
  public Throwable cause() {
    return cause;
  }

  @Override
  public boolean isSuccess() {
    return false;
  }

  @Override
  public ChannelFuture sync() {
    PlatformDependent.throwException(cause);
    return this;
  }

  @Override
  public ChannelFuture syncUninterruptibly() {
    PlatformDependent.throwException(cause);
    return this;
  }
}
