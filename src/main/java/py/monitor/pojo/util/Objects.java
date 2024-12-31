
package py.monitor.pojo.util;

public class Objects {
  private Objects() {
  }

  public static <E> E firstNotNull(E... all) {
    for (E element : all) {
      if (element != null) {
        return element;
      }
    }
    throw new NullPointerException("All null arguments");
  }
}
