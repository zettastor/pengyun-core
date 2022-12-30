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

package py.common.log.info.carrier;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import py.common.LoggerTracer;

public class TwoLogInfoCarrier implements LogInfoCarrier {
  private String format;
  private Object object1;
  private Object object2;

  public TwoLogInfoCarrier(String format, Object object1, Object object2) {
    this.format = format;
    this.object1 = object1;
    this.object2 = object2;
  }

  @Override
  public String buildLogInfo() {
    FormattingTuple ft = MessageFormatter.format(format, object1, object2);
    return LoggerTracer.buildString(ft);
  }

  @Override
  public void release() {
    this.format = null;
    this.object1 = null;
    this.object2 = null;
  }

}
