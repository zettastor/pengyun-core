
package py.datanode.checksecondaryinactive;

public interface CheckSecondaryInactive {
  boolean missTooManyPages(long missPages);

  boolean missTooManyLogs(long missLogs);

  boolean waitTimeout(long waitTime);

  CheckSecondaryInactiveThresholdMode getCheckMode();

  enum CheckSecondaryInactiveThresholdMode {
    AbsoluteTime,
    RelativeTime,
  }
}
