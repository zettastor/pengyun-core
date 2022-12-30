/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
