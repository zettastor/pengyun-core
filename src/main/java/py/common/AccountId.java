

package py.common;

/**
 * account id bean.
 */
public class AccountId {
  private long accountId;

  public AccountId(long id) {
    this.accountId = id;
  }

  public AccountId() {
  }

  public static long getDefaultId() {
    return Constants.SUPERADMIN_ACCOUNT_ID;
  }

  public long getId() {
    return accountId;
  }

  public void setId(long accountId) {
    this.accountId = accountId;
  }

}
