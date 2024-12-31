
package py.datanode.checksecondaryinactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSecondaryInactiveByRelativeTime extends CheckSecondaryInactiveByTime {
  private static final Logger logger = LoggerFactory
      .getLogger(CheckSecondaryInactiveByRelativeTime.class);
  private long waitTimerThreshold;

  public CheckSecondaryInactiveByRelativeTime(boolean ignoreMissPagesAndLogs,
      long waitTimerThreshold) {
    super(ignoreMissPagesAndLogs);
    this.waitTimerThreshold = waitTimerThreshold;
  }

  @Override
  public boolean waitTimeout(long waitTime) {
    if (waitTime >= waitTimerThreshold) {
      return true;
    }
    return false;
  }

  @Override
  public CheckSecondaryInactiveThresholdMode getCheckMode() {
    return CheckSecondaryInactiveThresholdMode.RelativeTime;
  }

  public void updateInfo(boolean ignoreMissPagesAndLogs, long waitTimerThreshold) {
    this.waitTimerThreshold = waitTimerThreshold;
    super.setIgnoreMissPagesAndLogs(ignoreMissPagesAndLogs);
  }
}
