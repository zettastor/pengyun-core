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

package py.exception;

public class TooManyEndPointFoundException extends Exception {
  private static final long serialVersionUID = 1L;

  public TooManyEndPointFoundException() {
    super();
  }

  public TooManyEndPointFoundException(String message) {
    super(message);
  }

  public TooManyEndPointFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public TooManyEndPointFoundException(Throwable cause) {
    super(cause);
  }
}
