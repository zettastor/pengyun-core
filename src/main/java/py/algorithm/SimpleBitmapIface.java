
package py.algorithm;

public interface SimpleBitmapIface {
  void set(int index);

  boolean get(int index);

  void clear(int index);

  void inverse();

  int size();
}
