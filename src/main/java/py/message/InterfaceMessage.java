
package py.message;

import java.util.UUID;

public interface InterfaceMessage<DataT> {
  public UUID uuid();

  public String name();

  public DataT getData();

  public void setData(DataT data);
}
