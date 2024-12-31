
package py.monitor.common;

import java.util.Set;

public class StartedPerformanceTask {
  Set<PerformanceTask> performanceTaskSet;

  public Set<PerformanceTask> getPerformanceTaskSet() {
    return performanceTaskSet;
  }

  public void setPerformanceTaskSet(Set<PerformanceTask> performanceTaskSet) {
    this.performanceTaskSet = performanceTaskSet;
  }
}
