

package py.io.sequential;

import java.util.List;

public interface RandomSequentialIdentifierV2 {
  void judgeIoIsSequential(List<? extends IoSequentialTypeHolder> ioContextList);
}
