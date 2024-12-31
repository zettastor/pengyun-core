
package py.monitor;

import org.junit.Test;
import py.monitor.common.AlertRule;
import py.monitor.common.CounterName;

public class CommonTest {
  @Test
  public void testAlertRuleWithCounterType() {
    AlertRule alertRule1 = new AlertRule();
    alertRule1.setCounterKey(CounterName.MEMORY.toString());

    AlertRule alertRule2 = new AlertRule();
    alertRule2.setCounterKey(CounterName.CPU.toString());

    AlertRule alertRule3 = new AlertRule();
    alertRule3.setCounterKey(CounterName.DISK_STATUS.toString());

    AlertRule alertRule4 = new AlertRule();
    alertRule4.setCounterKey("CPUMEMORY");
  }
}
