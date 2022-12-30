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

package py.connection.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;
import py.netty.core.ProtocolFactory;
import py.netty.core.TransferenceConfiguration;

public interface BootstrapBuilder {
  void setCfg(TransferenceConfiguration cfg);

  void setHashedWheelTimer(HashedWheelTimer hashedWheelTimer);

  void setProtocolFactory(ProtocolFactory protocolFactory);

  Bootstrap build();

  void setAllocator(ByteBufAllocator allocator);

  void setIoEventGroup(EventLoopGroup ioEventGroup);
}
