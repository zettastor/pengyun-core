
package py.processmanager.exception;

public class PmdbPathNotExist extends Exception {
  public PmdbPathNotExist() {
    super();
  }

  public PmdbPathNotExist(String err) {
    super(err);
  }

  public PmdbPathNotExist(String err, Throwable throwable) {
    super(err, throwable);
  }

  public PmdbPathNotExist(Throwable throwable) {
    super(throwable);
  }

}
