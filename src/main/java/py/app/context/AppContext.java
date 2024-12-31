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

package py.app.context;

import java.util.Map;
import py.common.struct.EndPoint;
import py.instance.Group;
import py.instance.InstanceDomain;
import py.instance.InstanceId;
import py.instance.InstanceStatus;
import py.instance.Location;
import py.instance.PortType;

public interface AppContext {
  /**
   * every instance has main service, if you want to get EndPoint of this service, this function can
   * do it.
   */
  public EndPoint getMainEndPoint();

  public InstanceId getInstanceId();

  public void setInstanceId(InstanceId instanceId);

  public Group getGroup();

  public void setGroupInfo(Group group) throws Exception;

  public String getInstanceName();

  public InstanceStatus getStatus();

  public void setStatus(InstanceStatus status);

  public Location getLocation();

  public EndPoint getEndPointByServiceName(PortType serviceNameEnumValue);

  public EndPoint getHeartbeatMyselfEndPoint();

  /**
   * Get all EndPoints already put inside.
   *
   * @return all EndPoints
   */
  public Map<PortType, EndPoint> getEndPoints();

  /**
   * A service may have several EndPoints, only some of them will be listened to by thrift.
   *
   * @return EndPoints Thrift need to listen to.
   */
  public Map<PortType, EndPoint> getEndPointsThrift();

  /**
   * maybe a process contain several services, so we should set the address (equal to EndPoint) to
   * supply service, and when we not set the host name, then we will select a default host name.
   */
  public void putEndPoint(PortType serviceNameEnumValue, EndPoint endPoint);

  public InstanceDomain getInstanceDomain();

  public void setInstanceDomain(InstanceDomain instanceDomain);

}
