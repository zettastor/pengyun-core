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

public class OneLogInfoCarrier implements LogInfoCarrier {
  private String format;
  private Object object;

  public OneLogInfoCarrier(String format, Object object) {
    this.format = format;
    this.object = object;
  }

  @Override
  public String buildLogInfo() {
    FormattingTuple ft = MessageFormatter.format(format, object);
    return LoggerTracer.buildString(ft);
  }

  @Override
  public void release() {
    this.format = null;
    this.object = null;
  }
}
