

package py.connection.pool.udp;

public interface UdpServer {
  void startEchoServer() throws Exception;

  void stopEchoServer();
}
