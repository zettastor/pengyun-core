

package py.connection.pool;

import io.netty.channel.Channel;

public interface ConnectionListener {
  public void complete(Channel channel);
}
