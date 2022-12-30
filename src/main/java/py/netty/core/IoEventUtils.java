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

package py.netty.core;

public class IoEventUtils {
  public static int calculateThreads(IoEventThreadsMode threadsMode, Float parameter) {
    switch (threadsMode) {
      case Calculate_From_Available_Core:
        return calculateThreadsByAvailableCores(parameter);
      case Fix_Threads_Mode:
        return parameter.intValue();
      default:
        return Runtime.getRuntime().availableProcessors();
    }
  }

  private static int calculateThreadsByAvailableCores(Float parameter) {
    int cores = Runtime.getRuntime().availableProcessors();
    return Math.max(1, (int) (parameter * cores));
  }
}
