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

package py.client;

import java.lang.reflect.Constructor;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.client.thrift.GenericThriftClientFactory;
import py.common.struct.EndPoint;
import py.exception.EndPointNotFoundException;
import py.exception.GenericThriftClientFactoryException;
import py.exception.TooManyEndPointFoundException;
import py.instance.Instance;
import py.instance.InstanceId;
import py.instance.InstanceStatus;
import py.instance.InstanceStore;
import py.instance.PortType;

public abstract class ClientWrapperFactory<ClientT, ClientWrapperT> {
  protected static final long DEFAULT_REQUEST_TIMEOUT = 20000L;
  private static final Logger logger = LoggerFactory.getLogger(ClientWrapperFactory.class);
  protected GenericThriftClientFactory<ClientT> genericClientFactory;
  protected Class<? extends ClientT> clientClass = null;
  protected Class<? extends ClientWrapperT> clientWrapperClass = null;
  protected boolean delegatable = false;
  protected InstanceStore instanceStore;
  protected InstanceId instanceId;
  protected String instanceName;

  protected abstract void init();

  public ClientWrapperT build() throws EndPointNotFoundException, TooManyEndPointFoundException,
      GenericThriftClientFactoryException {
    EndPoint endPoint = getEndpoint();
    return build(endPoint);
  }

  public ClientWrapperT build(boolean delegatable) throws EndPointNotFoundException,
      TooManyEndPointFoundException, GenericThriftClientFactoryException {
    EndPoint endPoint = getEndpoint();
    return build(endPoint, DEFAULT_REQUEST_TIMEOUT, delegatable);
  }

  public ClientWrapperT build(long requestTimeout) throws GenericThriftClientFactoryException,
      EndPointNotFoundException, TooManyEndPointFoundException {
    EndPoint endPoint = getEndpoint();
    return build(endPoint, requestTimeout);
  }

  public ClientWrapperT build(EndPoint endpoint) throws GenericThriftClientFactoryException {
    return build(endpoint, DEFAULT_REQUEST_TIMEOUT);
  }

  public ClientWrapperT build(EndPoint endpoint, long requestTimeout)
      throws GenericThriftClientFactoryException {
    return build(endpoint, requestTimeout, delegatable);
  }

  @SuppressWarnings("unchecked")
  public ClientWrapperT build(EndPoint endpoint, long requestTimeout, boolean delegatable)
      throws GenericThriftClientFactoryException {
    Validate.notNull(clientClass);
    Validate.notNull(clientWrapperClass);

    ClientT client = genericClientFactory
        .generateSyncClient(endpoint, requestTimeout, delegatable);
    ClientWrapperT clientWrapper = null;

    try {
      Constructor<ClientWrapperT> wrapperConstructor;
      wrapperConstructor = (Constructor<ClientWrapperT>) this.clientWrapperClass
          .getConstructor(this.clientClass);
      clientWrapper = (ClientWrapperT) wrapperConstructor.newInstance(client);
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      throw new GenericThriftClientFactoryException();
    }

    return clientWrapper;
  }

  protected EndPoint getEndpoint() throws EndPointNotFoundException, TooManyEndPointFoundException {
    Validate.notNull(instanceStore);
    Validate.notNull(instanceName);

    Instance instance = null;
    if (instanceId != null) {
      instance = instanceStore.get(instanceId);
      if (instance != null && instance.getStatus() == InstanceStatus.HEALTHY) {
        return instance.getEndPointByServiceName(PortType.CONTROL);
      }
    }

    Set<Instance> instances = instanceStore.getAll(instanceName, InstanceStatus.HEALTHY);
    logger.debug("Get all info center instances: {}", instances);
    if (instances.size() == 0) {
      String errDescription = "can't find any ok instances named " + instanceName;
      logger.error("{}", errDescription);
      throw new EndPointNotFoundException(errDescription);
    }

    instance = instances.iterator().next();
    instanceId = instance.getId();
    return instance.getEndPointByServiceName(PortType.CONTROL);
  }

  public void close() {
    if (genericClientFactory != null) {
      logger.debug("we are going to shutdown info center client");
      genericClientFactory.close();
    }
  }

  public InstanceStore getInstanceStore() {
    return instanceStore;
  }

  public void setInstanceStore(InstanceStore instanceStore) {
    this.instanceStore = instanceStore;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public Class<? extends ClientT> getClientClass() {
    return clientClass;
  }

  public void setClientClass(Class<? extends ClientT> clientClass) {
    this.clientClass = clientClass;
  }

  public Class<? extends ClientWrapperT> getClientWrapperClass() {
    return clientWrapperClass;
  }

  public void setClientWrapperClass(Class<? extends ClientWrapperT> clientWrapperClass) {
    this.clientWrapperClass = clientWrapperClass;
  }

  public InstanceId getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(InstanceId instanceId) {
    this.instanceId = instanceId;
  }

  public boolean isDelegatable() {
    return delegatable;
  }

  public void setDelegatable(boolean delegatable) {
    this.delegatable = delegatable;
  }

  public GenericThriftClientFactory<ClientT> getGenericClientFactory() {
    return genericClientFactory;
  }
}
