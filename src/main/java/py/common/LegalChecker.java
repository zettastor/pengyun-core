
package py.common;

/**
 * This interface used to define a common function of checking.
 *
 */
public interface LegalChecker extends Checker {
  public boolean legal() throws Exception;
}
