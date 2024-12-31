/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
