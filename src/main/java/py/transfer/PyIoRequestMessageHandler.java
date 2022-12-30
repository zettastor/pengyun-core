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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyIoRequestMessageHandler extends SimpleChannelInboundHandler<PyIoMessage> {
  private static final Logger logger = LoggerFactory.getLogger(PyIoTcpServer.class);
  BlockingQueue<PyIoMessage> requestMessageQueue;

  public PyIoRequestMessageHandler(BlockingQueue<PyIoMessage> queue) {
    this.requestMessageQueue = queue;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, PyIoMessage msg) throws Exception {
    logger.debug("receive a msg {}", msg);
    requestMessageQueue.put(msg);
  }

}
