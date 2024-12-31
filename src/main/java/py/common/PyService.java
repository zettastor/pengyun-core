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

import java.nio.file.Path;

/**
 * A collection of defined name for each service and instance in pengyun storage system. Service
 * name in the system is unique and constant.
 */
public enum PyService {
  /**
   * console.
   */
  CONSOLE("Console", "pengyun-console", "startup.sh", "org.apache.catalina.startup.Bootstrap"),


  /**
   * infocenter.
   */
  INFOCENTER("InfoCenter", "pengyun-infocenter", "start_infocenter.sh", "py.infocenter.Launcher"),


  /**
   * datanode.
   */
  DATANODE("DataNode", "pengyun-datanode", "start_datanode.sh", "py.datanode.Launcher"),

  /**
   * driver container.
   */
  DRIVERCONTAINER("DriverContainer", "pengyun-drivercontainer", "start_drivercontainer.sh",
      "py.drivercontainer.Launcher"),

  /**
   * dih.
   */
  DIH("DIH", "pengyun-instancehub", "start_dih.sh", "py.dih.Launcher"),

  /**
   * coordinator.
   */
  COORDINATOR("Coordinator", "pengyun-coordinator", "", "py.coordinator.Launcher"),

  DEPLOYMENTDAMON("DeploymentDamon", "pengyun-deploymentdamon", "", "");

  private String serviceName;

  private String serviceLauchingScriptName;

  private String serviceProjectKeyName;

  private String serviceMainClassName;

  private PyService(String serviceName, String serviceProjectKeyName,
      String serviceLauchingScriptName,
      String serviceMainClassName) {
    this.serviceName = serviceName;
    this.serviceProjectKeyName = serviceProjectKeyName;
    this.serviceLauchingScriptName = serviceLauchingScriptName;
    this.serviceMainClassName = serviceMainClassName;
  }

  private PyService(PyService pyService) throws Exception {
    if (pyService == null) {
      throw new Exception();
    }

    this.serviceName = pyService.serviceName;
    this.serviceProjectKeyName = pyService.serviceProjectKeyName;
    this.serviceLauchingScriptName = pyService.serviceLauchingScriptName;
    this.serviceMainClassName = pyService.serviceMainClassName;
  }

  /**
   * Find the enum value of a given name.
   *
   * @return null: cannot find the enum value of the name enum value   *
   */
  public static PyService findValueByName(String name) {
    name = name.toUpperCase();

    try {
      return Enum.valueOf(PyService.class, name);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * After deploy all service to remote machine, service is running under specific directory whose
   * name always contains project key name, such as "pengyun-datanode" etc. We find value by this
   * feature.
   *
   * @return null: no such service
   */
  public static PyService findValueByPath(Path path) {
    for (PyService service : values()) {
      if (path.toString().contains(service.getServiceProjectKeyName())) {
        return service;
      }
    }

    return null;
  }

  public static PyService findValueByServiceName(String serviceName) {
    if (serviceName == null) {
      return null;
    }

    for (PyService service : values()) {
      if (serviceName.equals(service.getServiceName())) {
        return service;
      }
    }

    return null;
  }

  /**
   * checkout if the service is leagle. <br> For now it only check the service name, you can define
   * more {@code Checker} to do any other checking.
   *
   * @param service service to be checked
   */
  public static LegalChecker is(PyService service) {
    return new NameChecker(service.getServiceName());
  }

  /**
   * checkout if the service is leagle.
   *
   * @param serviceName service name to be checked
   */
  public static LegalChecker is(String serviceName) {
    return new NameChecker(serviceName);
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getServiceLauchingScriptName() {
    return serviceLauchingScriptName;
  }

  public void setServiceLauchingScriptName(String serviceLauchingScriptName) {
    this.serviceLauchingScriptName = serviceLauchingScriptName;
  }

  public String getServiceProjectKeyName() {
    return serviceProjectKeyName;
  }

  public void setServiceProjectKeyName(String serviceProjectKeyName) {
    this.serviceProjectKeyName = serviceProjectKeyName;
  }

  public String getServiceMainClassName() {
    return serviceMainClassName;
  }

  public void setServiceMainClassName(String serviceMainClassName) {
    this.serviceMainClassName = serviceMainClassName;
  }

  public boolean isServiceConsole() {
    return serviceName.equals(CONSOLE.getServiceName());
  }

  /**
   * This class will be used to do some checking for client coders.
   *
   * <p>CAUTION: it not good enough to implement an anonymous {@code LegalChecker} because there
   * are more than 1 place to use the.
   */
  private static class NameChecker implements LegalChecker {
    private final String serviceName;

    public NameChecker(String serviceName) {
      this.serviceName = serviceName;
    }

    public boolean legal() throws Exception {
      for (PyService service : values()) {
        if (serviceName.equals(service.getServiceName())) {
          return true;
        }
      }
      return false;
    }
  }
}
