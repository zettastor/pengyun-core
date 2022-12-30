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

package py.filter;

import py.instance.Instance;
import py.instance.InstanceStatus;

public class UtilityInstanceFilters {
  public static class AndFilter implements InstanceFilter {
    final InstanceFilter[] filters;

    public AndFilter(InstanceFilter... filters) {
      this.filters = new InstanceFilter[filters.length];
      for (int i = 0; i < filters.length; i++) {
        this.filters[i] = filters[i];
      }
    }

    public boolean passed(Instance instance) {
      for (InstanceFilter filter : filters) {
        if (!filter.passed(instance)) {
          return false;
        }
      }
      return true;
    }
  }

  public static class OrFilter implements InstanceFilter {
    final InstanceFilter[] filters;

    public OrFilter(InstanceFilter... filters) {
      this.filters = filters;
    }

    public boolean passed(Instance instance) {
      for (InstanceFilter filter : filters) {
        if (filter.passed(instance)) {
          return true;
        }
      }
      return false;
    }
  }

  public static class NotFilter implements InstanceFilter {
    final InstanceFilter filter;

    public NotFilter(InstanceFilter filter) {
      this.filter = filter;
    }

    public boolean passed(Instance instance) {
      return !filter.passed(instance);
    }
  }

  public static class InstanceStatusFilter implements InstanceFilter {
    final InstanceStatus status;

    public InstanceStatusFilter(InstanceStatus state) {
      this.status = state;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getStatus() == null) {
        return false;
      }
      return instance.getStatus() == status;
    }
  }

  public static class InstanceStatusPrefixFilter implements InstanceFilter {
    final InstanceStatus state;
    final String prefix;

    public InstanceStatusPrefixFilter(InstanceStatus state, String prefix) {
      this.state = state;
      this.prefix = prefix;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getStatus() == null || instance.getId() == null) {
        return false;
      }
      return instance.getStatus() == state && instance.getId().toString().startsWith(prefix);
    }
  }

  public static class LocalDcInstanceStatusFilter implements InstanceFilter {
    final InstanceStatus state;
    final String dc;

    public LocalDcInstanceStatusFilter(String dc, InstanceStatus state) {
      this.dc = dc;
      this.state = state;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getLocation() == null
          || instance.getLocation().getDc() == null
          || instance.getStatus() == null) {
        return false;
      }
      return instance.getLocation().getDc().equals(this.dc) && instance.getStatus() == state;
    }
  }

  public static class ClusterInstanceStatusFilter implements InstanceFilter {
    final InstanceStatus state;
    final String cluster;

    public ClusterInstanceStatusFilter(String cluster, InstanceStatus state) {
      this.cluster = cluster;
      this.state = state;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getLocation() == null
          || instance.getLocation().getRegion() == null
          || instance.getStatus() == null) {
        return false;
      }
      return instance.getLocation().getRegion().equals(cluster) && instance.getStatus()
          .equals(state);
    }
  }

  public static class ClusterFilter implements InstanceFilter {
    final String cluster;

    public ClusterFilter(String cluster) {
      if (cluster == null) {
        throw new IllegalArgumentException("You can't pass a null cluster");
      }
      this.cluster = cluster;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getLocation() == null
          || instance.getLocation().getRegion() == null) {
        return false;
      }
      return instance.getLocation().getRegion().equals(cluster);
    }
  }

  public static class DcFilter implements InstanceFilter {
    final String dc;

    public DcFilter(String dc) {
      if (dc == null || dc.equals("")) {
        throw new IllegalArgumentException("You can't pass a null or empty dc string");
      }
      this.dc = dc;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getLocation() == null
          || instance.getLocation().getDc() == null) {
        return false;
      } else {
        return instance.getLocation().getDc().equals(dc);
      }
    }
  }

  public static class RackFilter implements InstanceFilter {
    final String rack;

    public RackFilter(String rack) {
      if (rack == null || rack.equals("")) {
        throw new IllegalArgumentException("You can't pass a null or empty rack string");
      }
      this.rack = rack;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getLocation() == null
          || instance.getLocation().getRack() == null) {
        return false;
      } else {
        return instance.getLocation().getRack().equals(rack);
      }
    }
  }

  public static class HostFilter implements InstanceFilter {
    final String host;

    public HostFilter(String host) {
      if (host == null || host.equals("")) {
        throw new IllegalArgumentException("You can't pass a null or empty host string");
      }
      this.host = host;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getLocation() == null
          || instance.getLocation().getHost() == null) {
        return false;
      } else {
        return instance.getLocation().getHost().equals(host);
      }
    }
  }

  public static class InstanceIdFilter implements InstanceFilter {
    final String instanceId;

    public InstanceIdFilter(String instanceId) {
      if (instanceId == null || instanceId.equals("")) {
        throw new IllegalArgumentException("You can't pass a null or empty instanceId string");
      }
      this.instanceId = instanceId;
    }

    public boolean passed(Instance instance) {
      if (instance == null || instance.getLocation() == null || instance.getId() == null) {
        return false;
      } else {
        return instance.getId().equals(instanceId);
      }
    }
  }
}
