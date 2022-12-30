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

package py.netty.client;

import py.netty.core.IoEventThreadsMode;
import py.netty.core.TransferenceOption;

public class TransferenceClientOption<T> extends TransferenceOption<T> {
  public static final TransferenceClientOption<Integer> IO_TIMEOUT_MS = valueOf("IO_TIMEOUT_MS");
  public static final TransferenceClientOption<Integer> IO_CONNECTION_TIMEOUT_MS
      = valueOf("IO_CONNECTION_TIMEOUT_MS");
  public static final TransferenceClientOption<Integer> MAX_LAST_TIME_FOR_CONNECTION_MS
      = valueOf("MAX_LAST_TIME_FOR_CONNECTION_MS");
  public static final TransferenceClientOption<Integer> CONNECTION_COUNT_PER_ENDPOINT
      = valueOf("CONNECTION_COUNT_PER_ENDPOINT");

  public static final TransferenceOption<IoEventThreadsMode> CLIENT_IO_EVENT_GROUP_THREADS_MODE
      = valueOf("CLIENT_IO_EVENT_GROUP_THREADS_MODE");
  public static final TransferenceOption<Float> CLIENT_IO_EVENT_GROUP_THREADS_PARAMETER
      = valueOf("CLIENT_IO_EVENT_GROUP_THREADS_PARAMETER");
  public static final TransferenceOption<IoEventThreadsMode> CLIENT_IO_EVENT_HANDLE_THREADS_MODE
      = valueOf("CLIENT_IO_EVENT_HANDLE_THREADS_MODE");
  public static final TransferenceOption<Float> CLIENT_IO_EVENT_HANDLE_THREADS_PARAMETER
      = valueOf("CLIENT_IO_EVENT_HANDLE_THREADS_PARAMETER");

  protected TransferenceClientOption(String name) {
    super(name);
  }

  private static <T> TransferenceClientOption<T> valueOf(String name) {
    return new TransferenceClientOption<T>(name);
  }
}
