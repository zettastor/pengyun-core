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

package py.netty.server;

import py.netty.core.IoEventThreadsMode;
import py.netty.core.TransferenceOption;

public class TransferenceServerOption<T> extends TransferenceOption<T> {
  public static final TransferenceServerOption<Integer> IO_SERVER_SO_BACKLOG = valueOf(
      "IO_SERVER_SO_BACKLOG");

  public static final TransferenceOption<IoEventThreadsMode> IO_EVENT_GROUP_THREADS_MODE = valueOf(
      "IO_EVENT_GROUP_THREADS_MODE");
  public static final TransferenceOption<Float> IO_EVENT_GROUP_THREADS_PARAMETER = valueOf(
      "IO_EVENT_GROUP_THREADS_PARAMETER");
  public static final TransferenceOption<IoEventThreadsMode> IO_EVENT_HANDLE_THREADS_MODE = valueOf(
      "IO_EVENT_HANDLE_THREADS_MODE");
  public static final TransferenceOption<Float> IO_EVENT_HANDLE_THREADS_PARAMETER = valueOf(
      "IO_EVENT_HANDLE_THREADS_PARAMETER");

  protected TransferenceServerOption(String name) {
    super(name);
  }

  private static <T> TransferenceServerOption<T> valueOf(String name) {
    return new TransferenceServerOption<T>(name);
  }

}
