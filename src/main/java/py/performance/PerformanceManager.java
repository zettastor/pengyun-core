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

package py.performance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceManager {
  private static PerformanceManager performanceManager;
  private Map<Long, PerformanceRecorder> performanceManagerMap = new ConcurrentHashMap<>();

  public static PerformanceManager getInstance() {
    synchronized (PerformanceRecorder.class) {
      if (performanceManager == null) {
        performanceManager = new PerformanceManager();
      }
    }
    return performanceManager;
  }

  public void addPerformance(long volumeId, PerformanceRecorder performanceRecorder) {
    performanceManagerMap.put(volumeId, performanceRecorder);
  }

  public void movePerformance(long volumeId) {
    performanceManagerMap.remove(volumeId);
  }

  public Map<Long, PerformanceRecorder> getPerformanceManagerMap() {
    return performanceManagerMap;

  }

  public void setPerformanceManagerMap(Map<Long, PerformanceRecorder> performanceManagerMap) {
    this.performanceManagerMap = performanceManagerMap;
  }

}
