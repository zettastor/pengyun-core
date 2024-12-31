

package py.transfer;

import io.netty.buffer.ByteBuf;

public class PyIoMessage {
  public static final int MAGIC_REQUEST = 0x48282150;
  public static final int MAGIC_RESPONSE = 0x48282151;

  private PyIoMessageHeader header;
  private ByteBuf body;

  public PyIoMessageHeader getHeader() {
    return header;
  }

  public void setHeader(PyIoMessageHeader header) {
    this.header = header;
  }

  public ByteBuf getBody() {
    return body;
  }

  public void setBody(ByteBuf body) {
    this.body = body;
  }

  public interface PyIoMessageContent {
    public ByteBuf getData();

    public void setData(ByteBuf data);
  }

}
