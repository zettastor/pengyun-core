
package py.connection.pool;

import py.common.struct.EndPoint;

public interface PyConnectionPool {
  PyConnection get(EndPoint endPoint);

  void close();

}
