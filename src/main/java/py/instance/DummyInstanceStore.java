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
