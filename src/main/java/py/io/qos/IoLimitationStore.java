
package py.io.qos;

import java.sql.Blob;
import java.util.List;

public interface IoLimitationStore {
  public void update(IoLimitation ioLimitationInformation);

  public void save(IoLimitation ioLimitationInformation);

  public IoLimitation get(long ioLimitationId);

  public List<IoLimitation> list();

  public int delete(long ioLimitationId);

  public Blob createBlob(byte[] bytes);
}
