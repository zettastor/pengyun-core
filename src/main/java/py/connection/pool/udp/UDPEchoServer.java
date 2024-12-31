
package py.connection.pool.udp;

public class UDPEchoServer {
  static {
    System.loadLibrary("udpServer");
  }

  public native int startEchoServer(int port);

  public native int stopEchoServer(int socketId);

  public native void pauseEchoServer();

  public native void reviveEchoServer();
}
