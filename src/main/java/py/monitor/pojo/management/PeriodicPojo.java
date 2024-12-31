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

package py.monitor.pojo.management;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.pojo.management.helper.NotificableDynamicMBean;

public abstract class PeriodicPojo extends TimerTask {
  private static final Logger logger = LoggerFactory.getLogger(PeriodicPojo.class);
  private NotificableDynamicMBean realBean;

  private boolean autoReport = false;

  public NotificableDynamicMBean getRealBean() {
    return realBean;
  }

  public void setRealBean(NotificableDynamicMBean realBean) {
    this.realBean = realBean;
  }

  public boolean isAutoReport() {
    return autoReport;
  }

  public void setAutoReport(boolean needReportOut) {
    this.autoReport = needReportOut;
  }

  @Override
  public void run() {
    try {
      modifyData();

      logger.debug("Reporter switch : {}", autoReport);
      if (autoReport == true) {
        realBean.sendPerformanceNotify();
      }
    } catch (Exception e) {
      logger.error("Caught an exception", e);
    }
  }

  public void stop() {
    this.cancel();
  }

  protected abstract void modifyData() throws Exception;

}
