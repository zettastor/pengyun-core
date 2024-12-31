

package py.netty.exception;

import static py.netty.exception.ExceptionType.NETWORK_UNHEALTHY_BY_SD;

import io.netty.buffer.ByteBuf;

public class NetworkUnhealthyBySdException extends AbstractNettyException {
  public NetworkUnhealthyBySdException(String msg) {
    super(NETWORK_UNHEALTHY_BY_SD, msg);
  }

  public NetworkUnhealthyBySdException() {
    super(NETWORK_UNHEALTHY_BY_SD);
  }

  public static AbstractNettyException fromBuffer(ByteBuf byteBuf) {
    return new NetworkUnhealthyBySdException();
  }
}
