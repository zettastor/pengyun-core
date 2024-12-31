
package py.storage.performance;

public interface SlowDiskChecker {
  void incomingIo();

  boolean isSlowDisk();

  boolean checking(String diskStatFile, String deviceName, int awaitThreshold);
}
