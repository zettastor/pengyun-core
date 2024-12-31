

package py.connection.pool.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;

public class NativeUdpEchoServer implements UdpServer {
  private static final Logger logger = LoggerFactory.getLogger(NioUdpEchoServer.class);
  private final EndPoint endPoint;
  private UDPEchoServer echoServerNative;
  private int fd = -1;

  public NativeUdpEchoServer(EndPoint endPoint) {
    this.endPoint = endPoint;
    this.echoServerNative = new UDPEchoServer();
  }

  public void startEchoServer() throws Exception {
    if (fd != -1) {
      logger.error("echo server has started, fd {}", fd);
      throw new Exception();
    }
    fd = echoServerNative.startEchoServer(endPoint.getPort());
    logger.warn("start server={}", fd);
  }

  public void stopEchoServer() {
    if (fd != -1) {
      echoServerNative.stopEchoServer(fd);
      logger.warn("stop server={}", fd);
      fd = -1;
    }
  }
}
