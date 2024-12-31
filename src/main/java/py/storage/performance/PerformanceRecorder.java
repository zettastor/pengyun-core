
package py.storage.performance;

import py.common.struct.Pair;

public interface PerformanceRecorder {
  Pair<Long, Long> getVal(double quantile, double ignorRatio);

  PerformanceContext startIo(Rw rw, long offset, int length);

  int getSize();

  void dump();
}
