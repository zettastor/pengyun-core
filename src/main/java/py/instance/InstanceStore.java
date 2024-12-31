

package py.instance;

import java.util.Set;
import py.common.struct.EndPoint;

public interface InstanceStore {
  public Set<Instance> getAll();

  public Set<Instance> getAll(String name);

  public Set<Instance> getAll(InstanceStatus status);

  public Set<Instance> getAll(String name, InstanceStatus status);

  public Instance get(InstanceId id);

  public Instance get(EndPoint endPoint);

  public void delete(Instance instance);

  public Instance getByHostNameAndServiceName(String hostName, String name);

  public void save(Instance instance);

  public void close();

}
