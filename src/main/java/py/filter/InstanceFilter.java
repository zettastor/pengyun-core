

package py.filter;

import py.instance.Instance;

public interface InstanceFilter {
  public boolean passed(Instance instance);
}
