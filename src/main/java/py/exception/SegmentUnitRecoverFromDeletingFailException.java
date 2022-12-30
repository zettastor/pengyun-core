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

public class SegmentUnitRecoverFromDeletingFailException extends Exception {
  private static final long serialVersionUID = 1L;
  private long volumeId;
  private int segIndex;
  private String errorInfo;

  public SegmentUnitRecoverFromDeletingFailException(long volumeId, int segIndex,
      String errorInfo) {
    this.volumeId = volumeId;
    this.segIndex = segIndex;
    this.errorInfo = errorInfo;
  }

  public SegmentUnitRecoverFromDeletingFailException() {
  }

  public SegmentUnitRecoverFromDeletingFailException(String errorInfo) {
    this.errorInfo = errorInfo;
  }

  public long getVolumeId() {
    return volumeId;
  }

  public int getSegIndex() {
    return segIndex;
  }

  public String getErrorInfo() {
    return errorInfo;
  }
}
