
package py.app.context;

import py.instance.InstanceId;

public interface InstanceIdStore {
  public InstanceId getInstanceId();

  public void persistInstanceId(InstanceId id);
}
