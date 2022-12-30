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

package py.monitor.utils;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.PyService;
import py.common.struct.EndPoint;
import py.instance.Instance;
import py.instance.InstanceStatus;
import py.instance.InstanceStore;

public class Utils {
  private static Logger logger = LoggerFactory.getLogger(Utils.class);

  public static String getServiceNameByEndPoint(EndPoint endpoint, InstanceStore instanceStore)
      throws Exception {
    String serviceName = "";
    Set<Instance> instances = instanceStore.getAll();
    boolean beFound = false;
    for (Instance instance : instances) {
      logger.debug("Current instance in instance-store is : {}", instance);
      if (instance.getEndPoint().equals(endpoint)) {
        serviceName = instance.getName();
        beFound = true;
        break;
      }
    }

    if (!beFound) {
      logger.warn("Can't find endpoint {} in instance-store : {}", endpoint, instances);
      throw new Exception();
    }
    return serviceName;
  }

  public static Set<Instance> getInstancesByServiceNames(Set<String> serviceNames,
      InstanceStore instanceStore) {
    Set<Instance> instances = new HashSet<Instance>();
    for (String serviceName : serviceNames) {
      instances.addAll(instanceStore.getAll(serviceName, InstanceStatus.HEALTHY));
    }
    return instances;
  }

  public static Set<EndPoint> getEndPointByServiceNames(Set<String> serviceNames,
      InstanceStore instanceStore)
      throws Exception {
    Set<EndPoint> endpoints = new HashSet<EndPoint>();
    for (String serviceName : serviceNames) {
      endpoints.addAll(getEndPointByServiceName(serviceName, instanceStore));
    }
    return endpoints;
  }

  public static Set<EndPoint> getEndPointByServiceName(String serviceName,
      InstanceStore instanceStore)
      throws Exception {
    if (!PyService.is(serviceName).legal()) {
      logger.error("Illegal service name: {}", serviceName);
      throw new Exception();
    }

    Set<EndPoint> endpoints = new HashSet<EndPoint>();
    Set<Instance> instances = instanceStore.getAll(serviceName, InstanceStatus.HEALTHY);

    for (Instance instance : instances) {
      endpoints.add(instance.getEndPoint());
    }

    return endpoints;
  }

}
