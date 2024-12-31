
package py.buffer;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;

public class PyBuffer {
  private ByteBuf byteBuf;

  public PyBuffer(ByteBuf byteBuf) {
    this.byteBuf = byteBuf;
  }

  public ByteBuf getByteBuf() {
    return byteBuf;
  }

  public ByteBuffer getByteBuffer() {
    return byteBuf.nioBuffer();
  }

  public void release() {
    if (byteBuf != null) {
      byteBuf.release();
      byteBuf = null;
    }
  }

  public void releaseReference() {
    byteBuf = null;
  }
}
