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

package py.netty.core.twothreads;

import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ThrowableUtil;
import org.slf4j.Logger;

public final class PromiseLoggingUtil {
  private PromiseLoggingUtil() {
  }

  public static void tryCancel(Promise<?> p, Logger logger) {
    if (!p.cancel(false) && logger != null) {
      Throwable err = p.cause();
      if (err == null) {
        logger.warn("Failed to cancel promise because it has succeeded already: {}", p);
      } else {
        logger.warn("Failed to cancel promise because it has failed already: {}, unnotified cause:",
            p, err);
      }
    }
  }

  public static <V> void trySuccess(Promise<? super V> p, V result, Logger logger) {
    if (!p.trySuccess(result) && logger != null) {
      Throwable err = p.cause();
      if (err == null) {
        logger.warn("Failed to mark a promise as success because it has succeeded already: {}", p);
      } else {
        logger.warn(
            "Failed to mark a promise as success because it has failed already: {}, unnotified"
                + " cause:", p, err);
      }
    }
  }

  public static void tryFailure(Promise<?> p, Throwable cause, Logger logger) {
    if (!p.tryFailure(cause) && logger != null) {
      Throwable err = p.cause();
      if (err == null) {
        logger.warn("Failed to mark a promise as failure because it has succeeded already: {}", p,
            cause);
      } else {
        logger.warn(
            "Failed to mark a promise as failure because it has failed already: {}, unnotified"
                + " cause: {}", p, ThrowableUtil.stackTraceToString(err), cause);
      }
    }
  }

}

