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

package py.transfer;

public final class PyIoParameters {
  public static final int MAX_CHANNELS = 256;
  public static final int SERVER_SO_BACKLOG = 256;
  public static final int MAX_MESSAGE_LENGTH = 5 * 1024 * 1024;
  public static final int LENGTH_FIELD_LENGTH = 4;
}
