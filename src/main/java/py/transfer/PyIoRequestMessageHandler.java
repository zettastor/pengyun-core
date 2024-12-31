

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
