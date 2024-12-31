
package py.test;

public class UdpServer {
  static {
    System.loadLibrary("udpServer");
  }

  public native int start(int post);

}

