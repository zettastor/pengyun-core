
package py.netty.message;

import io.netty.buffer.ByteBuf;

public interface MessageCarryDataInterface {
  ByteBuf getData();

  int getDataLength();
}
