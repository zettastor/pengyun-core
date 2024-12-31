
package py.engine;

public interface Latency {
  void mark();

  boolean isSafe();

  Latency createBranch();

  String print();
}
