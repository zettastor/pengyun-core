

package py.test;

public class UnitTestConfiguration extends TestConstants {
  private long accountId;

  private long volumeId;

  public static long getDefaultAccountId() {
    return AccountId4UnitTest;
  }

  public static long getDefaultVolumeId() {
    return VolumeId4UnitTest;
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
