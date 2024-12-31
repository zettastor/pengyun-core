
package py.common;

/**
 * function object.
 *
 * @param <ArgT> type of the argument
 */
public interface Functor<ArgT> {
  void invoke(ArgT arg) throws Exception;
}
