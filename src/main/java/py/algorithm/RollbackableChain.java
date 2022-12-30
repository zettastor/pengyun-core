/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
