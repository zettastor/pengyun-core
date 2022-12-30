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

package py.engine;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SingleTaskEngine extends AbstractTaskEngine {
  public SingleTaskEngine() {
    queue = new LinkedBlockingQueue<>();
    setPrefix("single-task");
  }

  public SingleTaskEngine(int maxTaskCount) {
    if (maxTaskCount > 0 && maxTaskCount < Integer.MAX_VALUE) {
      queue = new ArrayBlockingQueue<>(maxTaskCount);
    } else {
      queue = new LinkedBlockingQueue<>();
    }
  }

}
