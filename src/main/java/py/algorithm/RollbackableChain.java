
package py.algorithm;

import java.util.ArrayList;

public class RollbackableChain {
  private ArrayList<RollbackOnlyUnit> chainUnitList = new ArrayList<>();

  public void pushBackRollback(RollbackOnlyUnit unit) {
    chainUnitList.add(unit);
  }

  public void rollback() {
    for (int i = chainUnitList.size() - 1; i >= 0; i--) {
      chainUnitList.get(i).rollback();
    }

    chainUnitList.clear();
  }

  public void clear() {
    chainUnitList.clear();
  }

  public interface RollbackOnlyUnit {
    void rollback();
  }
}
