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
