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

package py.netty.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.exception.TooBigFrameException;
import py.netty.memory.PooledByteBufAllocatorWrapper;
import py.netty.message.Header;
import py.netty.message.Message;
import py.netty.message.MessageImpl;

public class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(ByteToMessageDecoder.class);

  private static final int DEFAULT_MAX_FRAME_SIZE = 1024 * 1024 * 16;
  private final ByteBufAllocator allocator;
  private final int maxFrameSize;
  private CompositeByteBuf cumulation;
  private ByteBuf data;
  private Header header;
  private int requestCount;

  public ByteToMessageDecoder() {
    this(DEFAULT_MAX_FRAME_SIZE, PooledByteBufAllocatorWrapper.INSTANCE);
  }

  public ByteToMessageDecoder(int maxFrameSize, ByteBufAllocator allocator) {
    this.maxFrameSize = maxFrameSize;
    this.cumulation = null;
    this.data = null;
    this.allocator = allocator;
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    logger.warn("deregister inactive channel:{}, header={}", ctx.channel(), header);
    ctx.fireChannelUnregistered();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    logger.warn("inactive channel:{}, header={}", ctx.channel(), header);
    header = null;
    if (cumulation != null) {
      cumulation.release();
      cumulation = null;
    }
    ctx.fireChannelInactive();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
   
    if (!(msg instanceof ByteBuf)) {
      ctx.fireChannelRead(msg);
      return;
    }

    data = (ByteBuf) msg;

    requestCount = 0;

    try {
      int processLength = 0;

      if (cumulation != null) {
        Validate.isTrue(cumulation.isReadable());
        processLength += processCumulation(ctx);

        logger
            .debug("after process cumulation and data, parsed request count {}, process length:{}",
                requestCount, processLength);
      }

      if (cumulation == null && data != null) {
       
        processLength += processData(ctx);
      } else {
       //
      }

      logger
          .debug("At last parsed request count {}, process length:{}", requestCount, processLength);

    } catch (Exception e) {
      logger.error("caught an exception", e);
      if (cumulation != null) {
        cumulation.release();
        cumulation = null;
      }
      if (data != null) {
        data.release();
        data = null;
      }
     
      ctx.fireChannelInactive();
    } finally {
      //
    }
  }

  private int processData(ChannelHandlerContext ctx) throws Exception {
    int processLength = 0;
    while (data.isReadable()) {
     
      if (header == null) {
        if (data.readableBytes() < Header.headerLength()) {
         
          break;
        } else {
         
          try {
            header = Header.fromBuffer(data);
            processLength += Header.headerLength();
            logger.trace("receive header:{}", header);
          } catch (Exception e) {
            logger.error("caught an exception, read index:{}, write index:{}, capacity:{}", data,
                data.readerIndex(), data.writerIndex(), data.capacity(), e);
            throw e;
          }

        }
      }

      Validate.notNull(header);

      int bodyLength = header.getLength();
     
      if (bodyLength == 0) {
        fireChanelRead(ctx, new MessageImpl(header, null));
        requestCount++;
        continue;
      }

      if (!data.isReadable()) {
       
        break;
      }

      int readableBytes = data.readableBytes();
      if (readableBytes < bodyLength) {
       
        break;
      } else {
       
        Message message = new MessageImpl(header,
            data.retainedSlice(data.readerIndex(), bodyLength));
        fireChanelRead(ctx, message);
        data.skipBytes(bodyLength);
        processLength += bodyLength;
        requestCount++;
      }
    }

    if (!data.isReadable()) {
      data.release();
      data = null;
    } else {
      Validate.isTrue(cumulation == null);
      cumulation = new CompositeByteBuf(allocator, true, 200);
      cumulation.addComponent(true, data);
    }

    return processLength;
  }

  private int processCumulation(ChannelHandlerContext ctx) throws Exception {
    int cumulationLength = cumulation.readableBytes();
    int processLength = 0;
   
    if (header == null) {
      cumulation.addComponent(true, data);
      if (cumulation.readableBytes() < Header.headerLength()) {
       
        data = null;
      } else {
       
        header = Header.fromBuffer(cumulation);
        processLength += Header.headerLength();
        logger.trace("receive header:{}", header);

        Validate.notNull(header);
        Validate.isTrue(processLength > cumulationLength);

        if (data.isReadable()) {
          data.retain();
          data.skipBytes(processLength - cumulationLength);
        } else {
          data = null;
        }
        cumulation.release();
        cumulation = null;

        if (header.getLength() == 0) {
          fireChanelRead(ctx, new MessageImpl(header, null));
          requestCount++;
        }
      }
    } else {
     
      int bodyLength = header.getLength();
     
      Validate.isTrue(bodyLength > 0);
      if (cumulation.readableBytes() + data.readableBytes() < bodyLength) {
       
        cumulation.addComponent(true, data);
        data = null;
      } else {
       
       
        int bodySizeInData = bodyLength - cumulation.readableBytes();

        ByteBuf tmpBuf = data.retainedSlice(data.readerIndex(), bodySizeInData);
       
        data.skipBytes(bodySizeInData);

        cumulation.addComponent(true, tmpBuf);
        final Message message = new MessageImpl(header, cumulation);
        cumulation = null;

        processLength += bodyLength;
        if (!data.isReadable()) {
          data.release();
          data = null;
        }

        fireChanelRead(ctx, message);
        requestCount++;
      }
    }
    return processLength;
  }

  public void fireChanelRead(ChannelHandlerContext ctx, Message message) {
   
    header = null;

    if (message.getHeader().getLength() > maxFrameSize) {
     
      logger.error("max frame size:{}, message:{}", maxFrameSize, message);
      message.release();
      ctx.fireExceptionCaught(
          new TooBigFrameException(maxFrameSize, message.getHeader().getLength())
              .setHeader(message.getHeader()));
    } else {
      logger.trace("complete header:{}", message.getHeader());
      ctx.fireChannelRead(message);
    }
  }

  public ByteBuf getCumulation() {
    return this.cumulation;
  }

  public Header getHeader() {
    return this.header;
  }

}
