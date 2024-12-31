/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
