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

package py.common;

import java.util.LinkedList;
import java.util.List;

public class CmdResult {
  private long retNo;
  private List<String> retMsgList = new LinkedList<>();
  private List<String> errMsgList = new LinkedList<>();

  public CmdResult() {
  }

  public boolean isOk() {
    return (retNo == 0);
  }

  public long getRetNo() {
    return retNo;
  }

  public void setRetNo(long retNo) {
    this.retNo = retNo;
  }

  public String getResStr() {
    return retMsgList.toString();
  }

  public String getErrStr() {
    return errMsgList.toString();
  }

  public List<String> getRetMsgList() {
    return retMsgList;
  }

  public void setRetMsgList(List<String> retMsgList) {
    this.retMsgList = retMsgList;
  }

  public List<String> getErrMsgList() {
    return errMsgList;
  }

  public void setErrMsgList(List<String> errMsgList) {
    this.errMsgList = errMsgList;
  }

  public void appendRetMsg(String msg) {
    this.retMsgList.add(msg);
  }

  public void appendErrMsg(String msg) {
    this.errMsgList.add(msg);
  }

  public CmdResult deepCopy(CmdResult other) {
    if (other == null) {
      return null;
    }

    if (other == this) {
      return this;
    }

    this.retNo = other.getRetNo();
    this.retMsgList.clear();
    this.retMsgList.addAll(other.getRetMsgList());
    this.errMsgList.clear();
    this.errMsgList.addAll(other.getErrMsgList());
    return this;
  }

  @Override
  public String toString() {
    return "CmdResult{"
        + "retNo=" + retNo
        + ", retMsgList=" + retMsgList
        + ", errMsgList=" + errMsgList
        + '}';
  }
}
