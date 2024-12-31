

package py.connection.pool;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionRequest implements ConnectionListener {
  public static final Logger logger = LoggerFactory.getLogger(ConnectionRequest.class);
  public final PyChannel pyChannel;

  public ConnectionRequest(PyChannel pyChannel) {
    this.pyChannel = pyChannel;
  }

  public PyChannel getPyChannel() {
    return pyChannel;
  }

  @Override
  public void complete(Channel channel) {
  }

  @Override
  public String toString() {
    return "ConnectionRequest{" + "pyChannel=" + pyChannel + '}';
  }
}
