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
