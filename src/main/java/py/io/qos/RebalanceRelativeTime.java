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

package py.io.qos;

public class RebalanceRelativeTime {
  private long waitTime;

  public long getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(long waitTime) {
    this.waitTime = waitTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RebalanceRelativeTime that = (RebalanceRelativeTime) o;

    return waitTime == that.waitTime;
  }

  @Override
  public int hashCode() {
    return (int) (waitTime ^ (waitTime >>> 32));
  }

  @Override
  public String toString() {
    return "RebalanceRelativeTime{"
        + "waitTime=" + waitTime
        + '}';
  }
}
