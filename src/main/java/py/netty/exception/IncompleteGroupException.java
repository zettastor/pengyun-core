
package py.netty.exception;

import io.netty.buffer.ByteBuf;

public class IncompleteGroupException extends AbstractNettyException {
  public IncompleteGroupException() {
    super(ExceptionType.INCOMPLETE_GROUP);
  }

  public static AbstractNettyException fromBuffer(ByteBuf byteBuf) {
    return new IncompleteGroupException();
  }
}
