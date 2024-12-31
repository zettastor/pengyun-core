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

package py.instance;

import java.util.HashSet;
import java.util.Set;
import py.common.struct.EndPoint;

public class DummyInstanceStore implements InstanceStore {
  private Set<Instance> instances = new HashSet<Instance>();

  public Set<Instance> getInstances() {
    return instances;
  }

  public void setInstances(Set<Instance> instances) {
    this.instances = instances;
  }

  @Override
  public synchronized void save(Instance instance) {
    instances.add(instance);
  }

  @Override
  public synchronized Set<Instance> getAll(String name, InstanceStatus status) {
    Set<Instance> returnedSet = new HashSet<Instance>();
    for (Instance instance : instances) {
      if (name.equals(instance.getName()) && status.equals(instance.getStatus())) {
        returnedSet.add(instance);
      }
    }

    return returnedSet;
  }

  @Override
  public synchronized Set<Instance> getAll(InstanceStatus status) {
    Set<Instance> returnedSet = new HashSet<Instance>();
    for (Instance instance : instances) {
      if (status.equals(instance.getStatus())) {
        returnedSet.add(instance);
      }
    }

    return returnedSet;
  }

  @Override
  public synchronized Set<Instance> getAll(String name) {
    Set<Instance> returnedSet = new HashSet<Instance>();
    for (Instance instance : instances) {
      if (name.equals(instance.getName())) {
        returnedSet.add(instance);
      }
    }

    return returnedSet;
  }

  @Override
  public synchronized Set<Instance> getAll() {
    return new HashSet<>(instances);
  }

  @Override
  public synchronized Instance get(EndPoint endPoint) {
    for (Instance instance : instances) {
      for (EndPoint tmp : instance.getEndPoints().values()) {
        if (tmp != null && tmp.equals(endPoint)) {
          return instance;
        }
      }
    }
    return null;
  }

  @Override
  public synchronized Instance get(InstanceId id) {
    for (Instance instance : instances) {
      if (instance.getId().equals(id)) {
        return instance;
      }
    }
    return null;
  }

  @Override
  public synchronized void delete(Instance instance) {
    instances.remove(instance);
  }

  @Override
  public Instance getByHostNameAndServiceName(String hostName, String name) {
    return null;
  }

  @Override
  public void close() {
  }

}
