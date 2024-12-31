
package py.connection.pool;

import py.netty.message.Message;

public interface PyConnection {
  public boolean isConnected();

  public void write(Message msg);

  public void writeAndFlush(Message msg);

  public void flush();
}
