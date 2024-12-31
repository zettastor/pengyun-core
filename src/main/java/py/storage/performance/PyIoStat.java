

package py.storage.performance;

public interface PyIoStat {
  long getIoAwait(IoStatElement elementPrevious, IoStatElement elementNow);

  IoStatElement readDiskStat(String fileName, String deviceName);
}
