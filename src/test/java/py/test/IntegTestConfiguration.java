
package py.test;

public class IntegTestConfiguration extends TestConstants {
  private long accountId;

  private long volumeId;

  public static long getDefaultVolumeId() {
    return VolumeId4IntegTest;
  }

  public long getAccountId() {
    return accountId;
  }

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  public long getVolumeId() {
    return volumeId;
  }

  public void setVolumeId(long volumeId) {
    this.volumeId = volumeId;
  }
}
