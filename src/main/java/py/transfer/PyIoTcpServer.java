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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyIoTcpServer {
  public static final int TCP_DATA_SERVER_PORT = 10012;
  private static final Logger logger = LoggerFactory.getLogger(PyIoTcpServer.class);
  final int serverThreadCount = 1;
  private BlockingQueue<PyIoMessage> requestMessageQueue;
  private PyChannelIdManager channelManager;
  private ServerBootstrap bootStrap;
  private EventLoopGroup serverGroup;

  public PyIoTcpServer(BlockingQueue<PyIoMessage> requestMessageQueue) {
    this.requestMessageQueue = requestMessageQueue;
    this.channelManager = new PyChannelIdManager(PyIoParameters.MAX_CHANNELS);
    this.serverGroup = new NioEventLoopGroup(serverThreadCount);
  }

  public void bind(int port) throws InterruptedException {
    bootStrap = new ServerBootstrap();
    bootStrap.group(serverGroup).channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, PyIoParameters.SERVER_SO_BACKLOG)
        .childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new PyIoConnectionHandler(channelManager));
            ch.pipeline().addLast(
                new PyIoMessageDecoder(PyIoParameters.MAX_MESSAGE_LENGTH, 4,
                    PyIoParameters.LENGTH_FIELD_LENGTH, channelManager));
            ch.pipeline().addLast(new PyIoMessageEncoder());
            ch.pipeline().addLast(new PyIoRequestMessageHandler(requestMessageQueue));
          }
        });

    bootStrap.bind(port).sync();
  }

  public void shutdown() {
    serverGroup.shutdownGracefully();
  }

  public PyChannelIdManager getChannelManager() {
    return channelManager;
  }

  private class PyIoConnectionHandler extends ChannelInboundHandlerAdapter {
    private PyChannelIdManager thechannelManager;

    public PyIoConnectionHandler(PyChannelIdManager channelManager) {
      super();
      this.thechannelManager = channelManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      super.channelActive(ctx);
      Channel channel = ctx.channel();

      thechannelManager.addChannel(channel);
      logger.info("channel is add {}", channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      super.channelInactive(ctx);

      Channel channel = ctx.channel();
      thechannelManager.delChannel(channel);
      logger.info("channel is del {}", channel);
    }

  }

  private class PyIoMessageEncoder extends MessageToMessageEncoder<PyIoMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, PyIoMessage msg, List<Object> out)
        throws Exception {
      ByteBuf buf = ctx.alloc().ioBuffer(msg.getHeader().getLength() + 8);

      buf.writeInt(msg.getHeader().getMagic());
      buf.writeInt(msg.getHeader().getLength());
      logger
          .warn("a msg is write length:{} requestid:{} channel id:{}", msg.getHeader().getLength(),
              msg.getHeader().getRequestid(), msg.getHeader().getChannelid());
      buf.writeLong(msg.getHeader().getRequestid());
      buf.writeInt(msg.getHeader().getChannelid());

      ByteBuf body = msg.getBody();

      buf.writeBytes(body, body.readerIndex(), body.readableBytes());
      logger.warn("a msg is write out {}", buf.readableBytes());
      out.add(buf);
    }

  }

  private class PyIoMessageDecoder extends LengthFieldBasedFrameDecoder {
    private PyChannelIdManager channelManager;

    public PyIoMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
        PyChannelIdManager channelManager) {
      super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
      this.channelManager = channelManager;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
      logger.info("server recieve msg :{}", in.readableBytes());
      ByteBuf packet = (ByteBuf) super.decode(ctx, in);
      if (packet == null) {
        logger.info("server recieve msg not long engouh:{}", in.readableBytes());
        return null;
      }
      logger.info("server recieve packet long engouh:{}", packet.readableBytes());
      final PyIoMessage message = new PyIoMessage();
      PyIoMessageHeader header = new PyIoMessageHeader();
      header.setMagic(packet.readInt());
      header.setLength(packet.readInt());
      header.setRequestid(packet.readLong());
      logger.info("channel id:{}", channelManager.getId(ctx.channel()));
      packet.readInt();
      header.setChannelid(channelManager.getId(ctx.channel()));

      message.setHeader(header);

      message.setBody(Unpooled.copiedBuffer(packet.slice()));

      logger.info("server decode message:{}", message);
      return message;
    }
  }

}
