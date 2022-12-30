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
