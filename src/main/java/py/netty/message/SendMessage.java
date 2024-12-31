
package py.netty.message;

import com.google.protobuf.AbstractMessageLite;
import io.netty.buffer.ByteBuf;
import py.netty.core.MethodCallback;

public class SendMessage extends MessageImpl {
  private final MethodCallback<AbstractMessageLite> callback;

  public SendMessage(Header header, ByteBuf buffer, MethodCallback<AbstractMessageLite> callback) {
    super(header, buffer);
    this.callback = callback;
  }

  public MethodCallback<AbstractMessageLite> getCallback() {
    return callback;
  }
}
