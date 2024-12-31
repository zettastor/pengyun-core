
package py.io.qos;

import py.periodic.UnableToStartException;

public interface IoLimitManager {
  public void updateLimitationsAndOpen(IoLimitationEntry ioLimitationEntry)
      throws UnableToStartException;

  public void close();

  public boolean isOpen();

  public IoLimitationEntry getIoLimitationEntry();

  public void tryGettingAnIo();

  public void tryThroughput(long size);

  public void slowDownExceptFor(long volumeId, int level);

  public void resetSlowLevel(long volumeId);

}
