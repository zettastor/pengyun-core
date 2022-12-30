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

package netty.transfer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ResourceLeakDetector;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import py.netty.core.ByteToMessageDecoder;
import py.netty.memory.SimplePooledByteBufAllocator;
import py.netty.message.Header;
import py.netty.message.Message;
import py.netty.message.MessageImpl;
import py.test.TestBase;

public class ByteToMessageDecoderTest extends TestBase {
  private static final int POOL_SIZE = 1024 * 1024 * 128;
  private static final int PAGE_SIZE = 8 * 1024;

  private SimplePooledByteBufAllocator allocator = new SimplePooledByteBufAllocator(POOL_SIZE,
      PAGE_SIZE);
  private int littlePageCount;
  private int pageCount;
  private ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

  public ByteToMessageDecoderTest() {
    littlePageCount = allocator.getAvailableLittlePageCount();
    pageCount = allocator.getAvailableMediumPageCount();
  }

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("io.netty.leakDetectionLevel", ResourceLeakDetector.Level.PARANOID.name());
    System.setProperty("io.netty.leakDetection.targetRecords", "30");
    System.setProperty("io.netty.leakDetection.maxRecords", "100");
    System.setProperty("io.netty.leakDetection.acquireAndReleaseOnly", "true");
    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

    Logger rootLogger = Logger.getRootLogger();
    rootLogger.setLevel(Level.DEBUG);
  }

  @Before
  public void beforeMethod() {
    String ip = "127.0.0.1";
    InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, 8080);
    Channel channel = mock(Channel.class);

    when(channel.remoteAddress()).thenReturn(inetSocketAddress);

    when(ctx.channel()).thenReturn(channel);
  }

  @After
  public void afterMethod() {
    assertEquals(littlePageCount, allocator.getAvailableLittlePageCount());
    assertEquals(pageCount, allocator.getAvailableMediumPageCount());
  }

  @Test
  public void testByteBufUsage18() {
    List<ByteBuf> byteBufList = new ArrayList<>();
    int length1 = 8 * 1024;
    int length2 = length1 / 2;
    byte[] bytes = new byte[length1];
    ByteBuf byteBuf1 = allocator.buffer(length1);
    byteBuf1.writeBytes(bytes);
    byteBufList.add(byteBuf1);
    ByteBuf byteBuf2 = allocator.buffer(length2);
    byteBuf2.writeBytes(bytes, 0, length2);
    byteBufList.add(byteBuf2);

    ByteBuf wrapperBuf = byteBuf1;

    wrapperBuf = Unpooled.wrappedBuffer(wrapperBuf, byteBuf2);

    assertEquals(1, wrapperBuf.refCnt());
    assertEquals(1, byteBuf1.refCnt());
    assertEquals(1, byteBuf2.refCnt());

    assertTrue(wrapperBuf.release());
    assertEquals(0, wrapperBuf.refCnt());

    assertEquals(1, byteBuf1.refCnt());
    assertEquals(0, byteBuf2.refCnt());
  }

  @Test
  public void decodeMessageOneByteByOneByte() throws Exception {
    int count = 2;
    Message[] responses = new Message[count];

    ByteToMessageDecoder decode = new ByteToMessageDecoder() {
      int i1 = 0;

      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        responses[i1++] = msg;

        super.fireChanelRead(ctx, msg);
      }
    };
    Message[] requests = new Message[count];
    ByteBuf bufferToSend = null;
    Random random = new Random();
    List<Integer> randomLengthList = new ArrayList<>();

    int extraIndex = random.nextInt(count - 1);
    for (int i = 0; i < (count - 1); i++) {
      if (i == extraIndex) {
        randomLengthList.add(0);
      }
      randomLengthList.add(random.nextInt(100));
    }

    List<ByteBuf> messageBufList = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      int randomLength = randomLengthList.get(i);
      requests[i] = generateMessage(i, randomLength);
      messageBufList.add(requests[i].getBuffer());
      if (bufferToSend == null) {
        bufferToSend = requests[i].getBuffer();
      } else {
        bufferToSend = Unpooled.wrappedBuffer(bufferToSend, requests[i].getBuffer());
      }
    }

    logger.warn("now random length list:{}", randomLengthList);
    int index = 0;
    while (index < bufferToSend.readableBytes()) {
      ByteBuf buf = bufferToSend.slice(index++, 1);
      buf.retain();
      decode.channelRead(ctx, buf);
    }

    for (int i = 0; i < count; i++) {
      assertEquals(requests[i].getHeader().getMethodType(),
          responses[i].getHeader().getMethodType());
      int bodyLength = requests[i].getHeader().getLength();
      if (bodyLength == 0) {
        assertEquals(responses[i].getBuffer(), null);
      } else {
        ByteBuf src = requests[i].getBuffer().slice(Header.headerLength(), bodyLength);
        ByteBuf des = responses[i].getBuffer();
        for (int j = 0; j < bodyLength; j++) {
          assertEquals(src.readByte(), des.readByte());
        }
        responses[i].release();
      }
    }

    assertTrue(bufferToSend.release());
    assertEquals(0, bufferToSend.refCnt());
  }

  @Test
  public void decodeMessageBlockByBlock() throws Exception {
    int count = 100;
    Message[] responses = new Message[count];
    List<Integer> randomLengthList = new ArrayList<>();
    List<Integer> randomStepList = new ArrayList<>();

    ByteToMessageDecoder decode = new ByteToMessageDecoder() {
      int i1 = 0;

      @Override
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        if (msg.getHeader().getDataLength() == 810) {
          logger.warn("breakpoint here");
        }
        responses[i1++] = msg;
        super.fireChanelRead(ctx, msg);
      }
    };
    Message[] requests = new Message[count];
    ByteBuf bufferToSend = null;
    Random random = new Random();
    for (int i = 0; i < count; i++) {
      int randomLength = random.nextInt(1011);
      randomLengthList.add(randomLength);

      requests[i] = generateMessage(i, randomLength);
      if (bufferToSend == null) {
        bufferToSend = requests[i].getBuffer();
      } else {
        bufferToSend = Unpooled.wrappedBuffer(bufferToSend, requests[i].getBuffer());
      }
    }

    int index = 0;
    while (index < bufferToSend.readableBytes()) {
      int randomStep = random.nextInt(PAGE_SIZE) + 1;
      randomStepList.add(randomStep);

      int leftSize = bufferToSend.readableBytes() - index;
      randomStep = Math.min(randomStep, leftSize);
      ByteBuf buf = bufferToSend.retainedSlice(index, randomStep);

      decode.channelRead(ctx, buf);
      index += randomStep;
    }

    for (int i = 0; i < count; i++) {
      assertEquals(requests[i].getHeader().getMethodType(),
          responses[i].getHeader().getMethodType());
      int bodyLength = requests[i].getHeader().getLength();
      if (bodyLength == 0) {
        assertEquals(responses[i].getBuffer(), null);
      } else {
        ByteBuf src = requests[i].getBuffer().slice(Header.headerLength(), bodyLength);
        ByteBuf des = responses[i].getBuffer();
        for (int j = 0; j < bodyLength; j++) {
          byte srcByte = src.readByte();
          byte desByte = 0;
          try {
            desByte = des.readByte();
          } catch (IllegalReferenceCountException e) {
            logger.error("can not happen, random length list:{}, random step list:{}",
                randomLengthList,
                randomStepList, e);
          }
          assertEquals(srcByte, desByte);
        }
      }
    }

    for (int i = 0; i < count; i++) {
      int bodyLength = responses[i].getHeader().getLength();
      if (bodyLength != 0) {
        responses[i].release();
      }
    }

    bufferToSend.release();
  }

  @Test
  public void decodeMessageBlockByBlockSpecially() throws Exception {
    List<Integer> randomLengthList = new ArrayList<>();
    randomLengthList.add(237);
    randomLengthList.add(766);
    randomLengthList.add(691);
    randomLengthList.add(810);
    randomLengthList.add(651);

    List<Integer> randomStepList = new ArrayList<>();
    randomStepList.add(1925);
    randomStepList.add(461);
    randomStepList.add(708);
    randomStepList.add(5885);

    int count = 5;
    Message[] responses = new Message[count];
    ByteToMessageDecoder decode = new ByteToMessageDecoder() {
      int i1 = 0;

      @Override
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        if (msg.getHeader().getDataLength() == 810) {
          logger.warn("breakpoint here");
        }
        responses[i1++] = msg;
        super.fireChanelRead(ctx, msg);
      }
    };
    Message[] requests = new Message[count];
    ByteBuf bufferToSend = null;
    for (int i = 0; i < count; i++) {
      int randomLength = randomLengthList.get(i);

      requests[i] = generateMessage(i, randomLength);
      if (bufferToSend == null) {
        bufferToSend = requests[i].getBuffer();
      } else {
        bufferToSend = Unpooled.wrappedBuffer(bufferToSend, requests[i].getBuffer());
      }
    }

    int index = 0;
    int loopIndex = 0;
    while (index < bufferToSend.readableBytes()) {
      int randomStep = randomStepList.get(loopIndex);
      int leftSize = bufferToSend.readableBytes() - index;
      randomStep = Math.min(randomStep, leftSize);
      ByteBuf buf = bufferToSend.retainedSlice(index, randomStep);

      decode.channelRead(ctx, buf);
      index += randomStep;
      loopIndex++;
    }

    for (int i = 0; i < count; i++) {
      assertEquals(requests[i].getHeader().getMethodType(),
          responses[i].getHeader().getMethodType());
      int bodyLength = requests[i].getHeader().getLength();
      if (bodyLength == 0) {
        assertEquals(responses[i].getBuffer(), null);
      } else {
        ByteBuf src = requests[i].getBuffer().slice(Header.headerLength(), bodyLength);
        ByteBuf des = responses[i].getBuffer();
        for (int j = 0; j < bodyLength; j++) {
          byte srcByte = src.readByte();
          byte desByte = 0;
          try {
            desByte = des.readByte();
          } catch (IllegalReferenceCountException e) {
            logger.error("can not happen, random length list:{}, random step list:{}",
                randomLengthList,
                randomStepList, e);
          }
          assertEquals(srcByte, desByte);
        }
      }
    }

    for (int i = 0; i < count; i++) {
      int bodyLength = responses[i].getHeader().getLength();
      if (bodyLength != 0) {
        responses[i].release();
      }
    }

    bufferToSend.release();
  }

  @Test
  public void allKindsOfMessage() throws Exception {
    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decode = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        if (msg.getHeader().getMethodType() == 10) {
          assertEquals(msg.getHeader().getLength(), 1024 * 1024);
        } else if (msg.getHeader().getLength() == 11) {
          assertEquals(msg.getHeader().getLength(), 57);
        }

        msg.release();
        count.incrementAndGet();
        super.fireChanelRead(ctx, msg);
      }
    };

    byte[] bigPack = generateMessageBytes(10, 1024 * 1024);
    byte[] litPack = generateMessageBytes(11, 57);

    Random random = new Random();
    int times = 1000;
    for (int i = 0; i < times; i++) {
      int useSize = 0;
      while (useSize < bigPack.length) {
        int size = PAGE_SIZE * random.nextInt(128) + 1;
        if (useSize + size >= bigPack.length) {
          int litCount = random.nextInt(4) + 1;
          int realSize = bigPack.length - useSize + litPack.length * litCount;
          ByteBuf buffer = allocator.buffer(realSize);
          buffer.writeBytes(bigPack, useSize, bigPack.length - useSize);
          for (int j = 0; j < litCount; j++) {
            buffer.writeBytes(litPack, 0, litPack.length);
          }
          decode.channelRead(ctx, buffer);
          useSize = bigPack.length;
          continue;
        }

        ByteBuf buffer = allocator.buffer(size);
        buffer.writeBytes(bigPack, useSize, size);
        decode.channelRead(ctx, buffer);
        useSize += size;
      }
    }
  }

  @Test
  public void testChannelRead0() {
    int messageIndex = 10;
    int bodyLen = 1024;
    ByteBuf messageByteBuf = generateMessageByteBuf(messageIndex, bodyLen);

    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();

        assertEquals(messageIndex, msg.getHeader().getMethodType());
        assertEquals(bodyLen, msg.getHeader().getLength());
        assertEquals(bodyLen, msg.getBuffer().readableBytes());
        validateOriginalMessageBufferAndParsedBuffer(messageByteBuf, msg.getBuffer(), bodyLen);

        msg.release();
        assertEquals(1, messageByteBuf.refCnt());
      }
    };

    try {
      decoder.channelRead(ctx, messageByteBuf);
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(0, messageByteBuf.refCnt());
      assertEquals(1, count.get());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testChannelRead0_0() {
    int messageIndex = 10;
    int bodyLen = 0;
    ByteBuf messageByteBuf = generateMessageByteBuf(messageIndex, bodyLen);

    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();

        assertEquals(messageIndex, msg.getHeader().getMethodType());
        assertEquals(bodyLen, msg.getHeader().getLength());
        assertEquals(null, msg.getBuffer());

        msg.release();
        assertEquals(1, messageByteBuf.refCnt());
      }
    };

    try {
      decoder.channelRead(ctx, messageByteBuf);
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(0, messageByteBuf.refCnt());
      assertEquals(1, count.get());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testChannelRead1() {
    int messageIndex = 10;
    int bodyLen = 1024;
    ByteBuf messageByteBuf = generateMessageByteBuf(messageIndex, bodyLen);
    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();
        assertEquals(messageIndex, msg.getHeader().getMethodType());
        assertEquals(bodyLen, msg.getHeader().getLength());
        assertEquals(bodyLen, msg.getBuffer().readableBytes());

        validateOriginalMessageBufferAndParsedBuffer(messageByteBuf, msg.getBuffer(), bodyLen);

        assertTrue(getCumulation() == null);
        msg.release();

        assertTrue(getCumulation() == null);

        assertEquals(2, messageByteBuf.refCnt());
      }
    };

    int messageLen = Header.headerLength() + bodyLen;
    int reduceCountForHeaderLen = 2;
    int part1Len = Header.headerLength() - reduceCountForHeaderLen;
    int part2Len = messageLen - part1Len;

    ByteBuf byteBufPart1 = messageByteBuf.retainedSlice(0, part1Len);
    ByteBuf byteBufPart2 = messageByteBuf.retainedSlice(part1Len, part2Len);
    assertEquals(3, messageByteBuf.refCnt());
    assertEquals(1, byteBufPart1.refCnt());
    assertEquals(1, byteBufPart2.refCnt());

    try {
      decoder.channelRead(ctx, byteBufPart1);
      assertEquals(1, byteBufPart1.refCnt());
      assertEquals(3, messageByteBuf.refCnt());
      assertTrue(decoder.getHeader() == null);
      assertEquals(part1Len, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(0, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart2);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(0, byteBufPart2.refCnt());
      assertEquals(1, messageByteBuf.refCnt());
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(1, count.get());
    } catch (Exception e) {
      fail();
    }

    assertTrue(messageByteBuf.release());
    assertEquals(0, messageByteBuf.refCnt());
  }

  private void testLeak() {
    for (int i = 0; i < 3000; i++) {
      ByteBuf messageByteBufTest = generateMessageByteBuf(10, 1024 * 128);
      assertEquals(1, messageByteBufTest.refCnt());
      messageByteBufTest = null;
    }
  }

  @Test
  public void testChannelRead2() {
    int messageIndex = 10;
    int bodyLen = 1024;
    ByteBuf messageByteBuf = generateMessageByteBuf(messageIndex, bodyLen);
    List<Message> responseList = new ArrayList<>();
    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();
        assertEquals(messageIndex, msg.getHeader().getMethodType());
        assertEquals(bodyLen, msg.getHeader().getLength());
        assertEquals(bodyLen, msg.getBuffer().readableBytes());

        validateOriginalMessageBufferAndParsedBuffer(messageByteBuf, msg.getBuffer(), bodyLen);

        assertNull(getCumulation());

        responseList.add(msg);

        assertEquals(2, getCumulation().refCnt());
        assertEquals(4, messageByteBuf.refCnt());
      }
    };

    int messageLen = Header.headerLength() + bodyLen;
    int lagerCountForHeaderLen = 2;
    int part1Len = Header.headerLength() + lagerCountForHeaderLen;
    int part2Len = 567;
    int part3Len = messageLen - part1Len - part2Len;

    ByteBuf byteBufPart1 = messageByteBuf.retainedSlice(0, part1Len);
    ByteBuf byteBufPart2 = messageByteBuf.retainedSlice(part1Len, part2Len);
    ByteBuf byteBufPart3 = messageByteBuf.retainedSlice(part1Len + part2Len, part3Len);

    byteBufPart1.markReaderIndex();
    validateOriginalMessageBufferAndParsedBuffer(messageByteBuf,
        Unpooled.wrappedBuffer(byteBufPart1.skipBytes(Header.headerLength()), byteBufPart2,
            byteBufPart3),
        bodyLen);
    byteBufPart1.resetReaderIndex();
    assertEquals(4, messageByteBuf.refCnt());
    assertEquals(1, byteBufPart1.refCnt());
    assertEquals(1, byteBufPart2.refCnt());
    assertEquals(1, byteBufPart3.refCnt());

    try {
      decoder.channelRead(ctx, byteBufPart1);
      assertEquals(1, byteBufPart1.refCnt());
      assertEquals(4, messageByteBuf.refCnt());
      assertTrue(decoder.getHeader() != null);
      assertEquals(lagerCountForHeaderLen, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(0, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart2);
      assertEquals(1, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(4, messageByteBuf.refCnt());
      assertTrue(decoder.getHeader() != null);
      assertEquals(lagerCountForHeaderLen + part2Len, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(0, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart3);

      responseList.get(0).getBuffer().resetReaderIndex();
      validateOriginalMessageBufferAndParsedBuffer(messageByteBuf, responseList.get(0).getBuffer(),
          bodyLen);

      responseList.get(0).release();

      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(0, byteBufPart2.refCnt());
      assertEquals(0, byteBufPart3.refCnt());
      assertEquals(1, messageByteBuf.refCnt());
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(1, count.get());

    } catch (Exception e) {
      logger.error("caught an exception", e);
      fail();
    }

    assertTrue(messageByteBuf.release());
    assertEquals(0, messageByteBuf.refCnt());
  }

  @Test
  public void testChannelRead3() {
    int messageAindex = 10;
    int bodyAlen = 1024;

    int messageBindex = 11;
    int bodyBlen = 1024 * 2;

    ByteBuf messageAbyteBuf = generateMessageByteBuf(messageAindex, bodyAlen);
    ByteBuf messageBbyteBuf = generateMessageByteBuf(messageBindex, bodyBlen);

    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();

        if (msg.getHeader().getMethodType() == messageAindex) {
          assertEquals(bodyAlen, msg.getHeader().getLength());
          assertEquals(bodyAlen, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageAbyteBuf, msg.getBuffer(), bodyAlen);

          assertTrue(getCumulation() == null);
          msg.release();
          assertTrue(getCumulation() == null);

          assertEquals(2, messageAbyteBuf.refCnt());
          assertEquals(2, messageBbyteBuf.refCnt());

        } else if (msg.getHeader().getMethodType() == messageBindex) {
          assertEquals(bodyBlen, msg.getHeader().getLength());
          assertEquals(bodyBlen, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageBbyteBuf, msg.getBuffer(), bodyBlen);

          assertTrue(getCumulation() == null);
          msg.release();
          assertTrue(getCumulation() == null);

          assertEquals(2, messageAbyteBuf.refCnt());
          assertEquals(2, messageBbyteBuf.refCnt());
        } else {
          fail();
        }
      }
    };

    final int messageAlen = Header.headerLength() + bodyAlen;

    int lagerCountForHeaderLen = 2;
    int part1Len = Header.headerLength() - lagerCountForHeaderLen;

    ByteBuf byteBufPart1 = messageAbyteBuf.retainedSlice(0, part1Len);
    final ByteBuf byteBufPart2 = Unpooled
        .wrappedBuffer(messageAbyteBuf.retainedSlice(part1Len, messageAlen - part1Len),
            messageBbyteBuf.retainedDuplicate());

    assertEquals(3, messageAbyteBuf.refCnt());
    assertEquals(2, messageBbyteBuf.refCnt());
    assertEquals(1, byteBufPart1.refCnt());
    assertEquals(1, byteBufPart2.refCnt());

    try {
      decoder.channelRead(ctx, byteBufPart1);
      assertEquals(1, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(3, messageAbyteBuf.refCnt());
      assertEquals(2, messageBbyteBuf.refCnt());
      assertTrue(decoder.getHeader() == null);
      assertEquals(part1Len, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(0, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart2);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(0, byteBufPart2.refCnt());
      assertEquals(1, messageAbyteBuf.refCnt());
      assertEquals(1, messageBbyteBuf.refCnt());
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(2, count.get());
    } catch (Exception e) {
      fail();
    }

    assertTrue(messageAbyteBuf.release());
    assertEquals(0, messageAbyteBuf.refCnt());
    assertTrue(messageBbyteBuf.release());
    assertEquals(0, messageBbyteBuf.refCnt());
  }

  @Test
  public void testChannelRead4() {
    int messageIndex = 10;
    int bodyLen = 1024;

    int messageIndex2 = 11;
    int bodyLen2 = 1024 * 2;

    ByteBuf messageByteBuf = generateMessageByteBuf(messageIndex, bodyLen);
    ByteBuf messageByteBuf2 = generateMessageByteBuf(messageIndex2, bodyLen2);

    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();

        if (msg.getHeader().getMethodType() == messageIndex) {
          assertEquals(bodyLen, msg.getHeader().getLength());
          assertEquals(bodyLen, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf, msg.getBuffer(), bodyLen);

          assertNull(getCumulation());
          assertEquals(3, messageByteBuf.refCnt());
          msg.release();

          assertEquals(2, messageByteBuf.refCnt());
          assertEquals(3, messageByteBuf2.refCnt());
        } else if (msg.getHeader().getMethodType() == messageIndex2) {
          assertEquals(bodyLen2, msg.getHeader().getLength());
          assertEquals(bodyLen2, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf2, msg.getBuffer(), bodyLen2);

          assertTrue(getCumulation() == null);
          msg.release();
          assertTrue(getCumulation() == null);

          assertEquals(1, messageByteBuf.refCnt());
          assertEquals(2, messageByteBuf2.refCnt());
        } else {
          fail();
        }
      }
    };

    int messageLen = Header.headerLength() + bodyLen;
    int messageLen2 = Header.headerLength() + bodyLen2;
    int totalMessageLen = messageLen + messageLen2;

    int lagerCountForHeaderLen = 2;
    int part1Len = Header.headerLength() + lagerCountForHeaderLen;
    int part2LenInA = (messageLen - part1Len);
    int part2LenInB = (Header.headerLength() - lagerCountForHeaderLen);
    int part2Len = part2LenInA + part2LenInB;
    int part3Len = totalMessageLen - part1Len - part2Len;

    ByteBuf byteBufPart1 = messageByteBuf.retainedSlice(0, part1Len);
    final ByteBuf byteBufPart2 = Unpooled
        .wrappedBuffer(messageByteBuf.retainedSlice(part1Len, part2LenInA),
            messageByteBuf2.retainedSlice(0, part2LenInB));
    final ByteBuf byteBufPart3 = messageByteBuf2.retainedSlice(part2LenInB, part3Len);

    assertEquals(3, messageByteBuf.refCnt());
    assertEquals(3, messageByteBuf2.refCnt());
    assertEquals(1, byteBufPart1.refCnt());
    assertEquals(1, byteBufPart2.refCnt());
    assertEquals(1, byteBufPart3.refCnt());

    try {
      decoder.channelRead(ctx, byteBufPart1);
      assertEquals(1, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(1, byteBufPart3.refCnt());
      assertEquals(3, messageByteBuf.refCnt());
      assertEquals(3, messageByteBuf2.refCnt());
      assertTrue(decoder.getHeader() != null);
      assertEquals(lagerCountForHeaderLen, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(0, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart2);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(1, byteBufPart3.refCnt());
      assertEquals(2, messageByteBuf.refCnt());
      assertEquals(3, messageByteBuf2.refCnt());
      assertTrue(decoder.getHeader() == null);
      assertEquals(part2LenInB, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(1, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart3);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(0, byteBufPart2.refCnt());
      assertEquals(0, byteBufPart3.refCnt());
      assertEquals(1, messageByteBuf.refCnt());
      assertEquals(1, messageByteBuf2.refCnt());
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(2, count.get());
    } catch (Exception e) {
      fail();
    }

    assertTrue(messageByteBuf.release());
    assertEquals(0, messageByteBuf.refCnt());
    assertTrue(messageByteBuf2.release());
    assertEquals(0, messageByteBuf2.refCnt());
  }

  @Test
  public void testChannelRead5() {
    int messageIndex = 10;
    int bodyLen = 1024;

    int messageIndex2 = 11;
    int bodyLen2 = 1024 * 2;

    int messageIndex3 = 12;
    int bodyLen3 = 1024 * 3;

    ByteBuf messageByteBuf = generateMessageByteBuf(messageIndex, bodyLen);
    ByteBuf messageByteBuf2 = generateMessageByteBuf(messageIndex2, bodyLen2);
    ByteBuf messageByteBuf3 = generateMessageByteBuf(messageIndex3, bodyLen3);

    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();

        if (msg.getHeader().getMethodType() == messageIndex) {
          assertEquals(bodyLen, msg.getHeader().getLength());
          assertEquals(bodyLen, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf, msg.getBuffer(), bodyLen);

          assertNull(getCumulation());

          assertEquals(3, messageByteBuf.refCnt());
          msg.release();
          assertEquals(2, messageByteBuf.refCnt());

          assertEquals(2, messageByteBuf2.refCnt());
          assertEquals(2, messageByteBuf3.refCnt());
        } else if (msg.getHeader().getMethodType() == messageIndex2) {
          assertEquals(bodyLen2, msg.getHeader().getLength());
          assertEquals(bodyLen2, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf2, msg.getBuffer(), bodyLen2);

          assertTrue(getCumulation() == null);
          msg.release();
          assertTrue(getCumulation() == null);

          assertEquals(2, messageByteBuf.refCnt());
          assertEquals(2, messageByteBuf2.refCnt());
          assertEquals(2, messageByteBuf3.refCnt());
        } else if (msg.getHeader().getMethodType() == messageIndex3) {
          assertEquals(bodyLen3, msg.getHeader().getLength());
          assertEquals(bodyLen3, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf3, msg.getBuffer(), bodyLen3);

          assertTrue(getCumulation() == null);
          msg.release();
          assertTrue(getCumulation() == null);

          assertEquals(1, messageByteBuf.refCnt());
          assertEquals(1, messageByteBuf2.refCnt());
          assertEquals(2, messageByteBuf3.refCnt());
        } else {
          fail();
        }
      }
    };

    int messageLen = Header.headerLength() + bodyLen;

    int lagerCountForHeaderLen = 2;
    int part1Len = Header.headerLength() + lagerCountForHeaderLen;
    int part2LenInA = (messageLen - part1Len);

    final ByteBuf byteBufPart1 = messageByteBuf.retainedSlice(0, part1Len);
    final ByteBuf byteBufPart2 = Unpooled
        .wrappedBuffer(messageByteBuf.retainedSlice(part1Len, part2LenInA),
            messageByteBuf2.retainedDuplicate());
    final ByteBuf byteBufPart3 = messageByteBuf3.retainedDuplicate();

    assertEquals(3, messageByteBuf.refCnt());
    assertEquals(2, messageByteBuf2.refCnt());
    assertEquals(2, messageByteBuf3.refCnt());
    assertEquals(1, byteBufPart1.refCnt());
    assertEquals(1, byteBufPart2.refCnt());
    assertEquals(1, byteBufPart3.refCnt());

    try {
      decoder.channelRead(ctx, byteBufPart1);
      assertEquals(1, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(1, byteBufPart3.refCnt());
      assertEquals(3, messageByteBuf.refCnt());
      assertEquals(2, messageByteBuf2.refCnt());
      assertEquals(2, messageByteBuf3.refCnt());
      assertTrue(decoder.getHeader() != null);
      assertEquals(lagerCountForHeaderLen, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(0, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart2);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(0, byteBufPart2.refCnt());
      assertEquals(1, byteBufPart3.refCnt());
      assertEquals(1, messageByteBuf.refCnt());
      assertEquals(1, messageByteBuf2.refCnt());
      assertEquals(2, messageByteBuf3.refCnt());
      assertTrue(decoder.getHeader() == null);
      assertTrue(decoder.getCumulation() == null);
      assertEquals(2, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart3);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(0, byteBufPart2.refCnt());
      assertEquals(0, byteBufPart3.refCnt());
      assertEquals(1, messageByteBuf.refCnt());
      assertEquals(1, messageByteBuf2.refCnt());
      assertEquals(1, messageByteBuf3.refCnt());
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(3, count.get());
    } catch (Exception e) {
      fail();
    }

    assertTrue(messageByteBuf.release());
    assertEquals(0, messageByteBuf.refCnt());
    assertTrue(messageByteBuf2.release());
    assertEquals(0, messageByteBuf2.refCnt());
    assertTrue(messageByteBuf3.release());
    assertEquals(0, messageByteBuf3.refCnt());
  }

  @Test
  public void testChannelRead6() {
    int messageIndex = 10;
    int bodyLen = 1024;

    int messageIndex2 = 11;
    int bodyLen2 = 1024 * 2;

    int messageIndex3 = 12;
    int bodyLen3 = 1024 * 3;

    ByteBuf messageByteBuf = generateMessageByteBuf(messageIndex, bodyLen);
    ByteBuf messageByteBuf2 = generateMessageByteBuf(messageIndex2, bodyLen2);
    ByteBuf messageByteBuf3 = generateMessageByteBuf(messageIndex3, bodyLen3);

    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();

        if (msg.getHeader().getMethodType() == messageIndex) {
          assertEquals(bodyLen, msg.getHeader().getLength());
          assertEquals(bodyLen, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf, msg.getBuffer(), bodyLen);

          assertNull(getCumulation());
          assertEquals(3, messageByteBuf.refCnt());
          msg.release();
          assertEquals(2, messageByteBuf.refCnt());

          assertEquals(2, messageByteBuf2.refCnt());
          assertEquals(3, messageByteBuf3.refCnt());
        } else if (msg.getHeader().getMethodType() == messageIndex2) {
          assertEquals(bodyLen2, msg.getHeader().getLength());
          assertEquals(bodyLen2, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf2, msg.getBuffer(), bodyLen2);

          assertTrue(getCumulation() == null);
          msg.release();
          assertTrue(getCumulation() == null);

          assertEquals(2, messageByteBuf.refCnt());
          assertEquals(2, messageByteBuf2.refCnt());
          assertEquals(3, messageByteBuf3.refCnt());
        } else if (msg.getHeader().getMethodType() == messageIndex3) {
          assertEquals(bodyLen3, msg.getHeader().getLength());
          assertEquals(bodyLen3, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf3, msg.getBuffer(), bodyLen3);

          assertTrue(getCumulation() == null);
          msg.release();
          assertTrue(getCumulation() == null);

          assertEquals(1, messageByteBuf.refCnt());
          assertEquals(1, messageByteBuf2.refCnt());
          assertEquals(2, messageByteBuf3.refCnt());
        } else {
          fail();
        }
      }
    };

    int messageLen = Header.headerLength() + bodyLen;
    int messageLen2 = Header.headerLength() + bodyLen2;
    int messageLen3 = Header.headerLength() + bodyLen3;
    int totalMessageLen = messageLen + messageLen2 + messageLen3;

    int lagerCountForHeaderLen = 2;
    int part1Len = Header.headerLength() + lagerCountForHeaderLen;
    int part2LenInA = (messageLen - part1Len);
    int part2LenInB = messageLen2;
    int part2LenInC = (Header.headerLength() - lagerCountForHeaderLen);
    int part2Len = part2LenInA + part2LenInB + part2LenInC;
    int part3Len = totalMessageLen - part1Len - part2Len;

    final ByteBuf byteBufPart1 = messageByteBuf.retainedSlice(0, part1Len);
    final ByteBuf byteBufPart2 = Unpooled
        .wrappedBuffer(messageByteBuf.retainedSlice(part1Len, part2LenInA),
            messageByteBuf2.retainedDuplicate(), messageByteBuf3.retainedSlice(0, part2LenInC));
    final ByteBuf byteBufPart3 = messageByteBuf3.retainedSlice(part2LenInC, part3Len);

    assertEquals(3, messageByteBuf.refCnt());
    assertEquals(2, messageByteBuf2.refCnt());
    assertEquals(3, messageByteBuf3.refCnt());
    assertEquals(1, byteBufPart1.refCnt());
    assertEquals(1, byteBufPart2.refCnt());
    assertEquals(1, byteBufPart3.refCnt());

    try {
      decoder.channelRead(ctx, byteBufPart1);
      assertEquals(1, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(1, byteBufPart3.refCnt());
      assertEquals(3, messageByteBuf.refCnt());
      assertEquals(2, messageByteBuf2.refCnt());
      assertEquals(3, messageByteBuf3.refCnt());
      assertTrue(decoder.getHeader() != null);
      assertEquals(lagerCountForHeaderLen, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(0, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart2);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(1, byteBufPart3.refCnt());
      assertEquals(2, messageByteBuf.refCnt());
      assertEquals(2, messageByteBuf2.refCnt());
      assertEquals(3, messageByteBuf3.refCnt());
      assertTrue(decoder.getHeader() == null);
      assertEquals(part2LenInC, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(2, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart3);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(0, byteBufPart2.refCnt());
      assertEquals(0, byteBufPart3.refCnt());
      assertEquals(1, messageByteBuf.refCnt());
      assertEquals(1, messageByteBuf2.refCnt());
      assertEquals(1, messageByteBuf3.refCnt());
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(3, count.get());
    } catch (Exception e) {
      fail();
    }

    assertTrue(messageByteBuf.release());
    assertEquals(0, messageByteBuf.refCnt());
    assertTrue(messageByteBuf2.release());
    assertEquals(0, messageByteBuf2.refCnt());
    assertTrue(messageByteBuf3.release());
    assertEquals(0, messageByteBuf3.refCnt());
  }

  @Test
  public void testChannelRead7() {
    int messageIndex = 2;
    int bodyLen = 1024;

    int messageIndex2 = 11;
    int bodyLen2 = 1024 * 2;

    ByteBuf messageByteBuf = generateMessageByteBuf(messageIndex, bodyLen);
    ByteBuf messageByteBuf2 = generateMessageByteBuf(messageIndex2, bodyLen2);

    AtomicInteger count = new AtomicInteger(0);
    ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void fireChanelRead(ChannelHandlerContext ctx, Message msg) {
        super.fireChanelRead(ctx, msg);
        count.incrementAndGet();

        if (msg.getHeader().getMethodType() == messageIndex) {
          assertEquals(bodyLen, msg.getHeader().getLength());
          assertEquals(bodyLen, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf, msg.getBuffer(), bodyLen);

          assertNull(getCumulation());
          assertEquals(3, messageByteBuf.refCnt());
          msg.release();
          assertEquals(2, messageByteBuf.refCnt());

          assertEquals(3, messageByteBuf2.refCnt());
        } else if (msg.getHeader().getMethodType() == messageIndex2) {
          assertEquals(bodyLen2, msg.getHeader().getLength());
          assertEquals(bodyLen2, msg.getBuffer().readableBytes());

          validateOriginalMessageBufferAndParsedBuffer(messageByteBuf2, msg.getBuffer(), bodyLen2);

          assertTrue(getCumulation() == null);
          msg.release();
          assertTrue(getCumulation() == null);

          assertEquals(1, messageByteBuf.refCnt());
          assertEquals(2, messageByteBuf2.refCnt());
        } else {
          fail();
        }
      }
    };

    int messageLen = Header.headerLength() + bodyLen;
    int messageLen2 = Header.headerLength() + bodyLen2;
    int totalMessageLen = messageLen + messageLen2;

    int lagerCountForHeaderLen = 2;
    int part1Len = Header.headerLength() + lagerCountForHeaderLen;
    int part2LenInA = (messageLen - part1Len);
    int part2LenInB = (Header.headerLength() - lagerCountForHeaderLen);
    int part2Len = part2LenInA + part2LenInB;
    int part3Len = totalMessageLen - part1Len - part2Len;

    ByteBuf byteBufPart1 = messageByteBuf.retainedSlice(0, part1Len);
    final ByteBuf byteBufPart2 = Unpooled
        .wrappedBuffer(messageByteBuf.retainedSlice(part1Len, part2LenInA),
            messageByteBuf2.retainedSlice(0, part2LenInB));
    final ByteBuf byteBufPart3 = messageByteBuf2.retainedSlice(part2LenInB, part3Len);

    assertEquals(3, messageByteBuf.refCnt());
    assertEquals(3, messageByteBuf2.refCnt());
    assertEquals(1, byteBufPart1.refCnt());
    assertEquals(1, byteBufPart2.refCnt());
    assertEquals(1, byteBufPart3.refCnt());

    try {
      decoder.channelRead(ctx, byteBufPart1);
      assertEquals(1, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(1, byteBufPart3.refCnt());
      assertEquals(3, messageByteBuf.refCnt());
      assertEquals(3, messageByteBuf2.refCnt());
      assertTrue(decoder.getHeader() != null);
      assertEquals(lagerCountForHeaderLen, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(0, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart2);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(1, byteBufPart2.refCnt());
      assertEquals(1, byteBufPart3.refCnt());
      assertEquals(2, messageByteBuf.refCnt());
      assertEquals(3, messageByteBuf2.refCnt());
      assertTrue(decoder.getHeader() == null);
      assertEquals(part2LenInB, decoder.getCumulation().readableBytes());
      assertEquals(1, decoder.getCumulation().refCnt());
      assertEquals(1, count.get());
    } catch (Exception e) {
      fail();
    }

    try {
      decoder.channelRead(ctx, byteBufPart3);
      assertEquals(0, byteBufPart1.refCnt());
      assertEquals(0, byteBufPart2.refCnt());
      assertEquals(0, byteBufPart3.refCnt());
      assertEquals(1, messageByteBuf.refCnt());
      assertEquals(1, messageByteBuf2.refCnt());
      assertTrue(decoder.getCumulation() == null);
      assertTrue(decoder.getHeader() == null);
      assertEquals(2, count.get());
    } catch (Exception e) {
      fail();
    }

    assertTrue(messageByteBuf.release());
    assertEquals(0, messageByteBuf.refCnt());
    assertTrue(messageByteBuf2.release());
    assertEquals(0, messageByteBuf2.refCnt());
  }

  private Message generateMessage(int index, int bodySize) {
    Header header = new Header((byte) index, 0, bodySize, index);
    ByteBuf buf = allocator.buffer(Header.headerLength() + bodySize);
    header.toBuffer(buf);
    for (int i = 0; i < bodySize; i++) {
      buf.writeByte(i);
    }

    return new MessageImpl(header, buf);
  }

  private byte[] generateMessageBytes(int index, int bodySize) {
    Header header = new Header((byte) index, 0, bodySize, index);
    byte[] buf = new byte[Header.headerLength() + bodySize];
    ByteBuffer buff = ByteBuffer.wrap(buf);
    header.toBuffer(buff);
    for (int i = 0; i < bodySize; i++) {
      buff.put((byte) i);
    }
    return buf;
  }

  private ByteBuf generateMessageByteBuf(int index, int bodySize) {
    ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
    Header header = new Header((byte) index, 0, bodySize, index);
    ByteBuf byteBuf = allocator.buffer(Header.headerLength() + bodySize);
    header.toBuffer(byteBuf);
    for (int i = 0; i < bodySize; i++) {
      byteBuf.writeByte((byte) i);
    }
    return byteBuf;
  }

  private void validateOriginalMessageBufferAndParsedBuffer(ByteBuf originalMessageBuffer,
      ByteBuf parsedMessageBuffer, int bodyLength) {
    ByteBuf src = originalMessageBuffer.slice(Header.headerLength(), bodyLength);
    ByteBuf des = parsedMessageBuffer;
    for (int j = 0; j < bodyLength; j++) {
      byte srcByte = src.readByte();
      byte desByte = des.readByte();
      assertEquals(srcByte, desByte);
    }
  }

  private void validateOriginalMessageAndParsedMessage(Message originalMessage,
      Message parsedMessage) {
    Validate.notNull(originalMessage);
    Validate.notNull(parsedMessage);
    Validate.isTrue(originalMessage.getHeader().equals(parsedMessage.getHeader()));
    int bodyLength = originalMessage.getHeader().getDataLength();
    validateOriginalMessageBufferAndParsedBuffer(originalMessage.getBuffer(),
        parsedMessage.getBuffer(),
        bodyLength);
  }

  private void validateTwoMessageList(List<Message> originalMessageList,
      List<Message> parsedMessageList) {
    Validate.notNull(originalMessageList);
    Validate.notNull(parsedMessageList);
    Validate.isTrue(originalMessageList.size() == parsedMessageList.size());

    int count = originalMessageList.size();

    if (count > 0) {
      for (int i = 0; i < count; i++) {
        validateOriginalMessageAndParsedMessage(originalMessageList.get(i),
            parsedMessageList.get(i));
      }
    }
  }

  @Test
  public void testHowToUseCompositeBuf() {
    ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

    CompositeByteBuf compositeByteBuf = new CompositeByteBuf(allocator, true, 5);
    ByteBuf[] bufs = new ByteBuf[20];

    for (int i = 0; i < 20; i++) {
      logger.warn(" " + i);
      ByteBuf data = PooledByteBufAllocator.DEFAULT.buffer(100);
      bufs[i] = data;
      data.writerIndex(100);

      data.retain();
      compositeByteBuf.addComponent(true, data);
      data.release();
    }

    compositeByteBuf.release();

    for (int i = 0; i < 20; i++) {
      assertEquals(0, bufs[i].refCnt());
    }
  }
}
