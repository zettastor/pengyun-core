

package py.connection.pool;

public interface ReconnectionHandler {
  public void reconnect(ConnectionRequest connectionRequest);
}
