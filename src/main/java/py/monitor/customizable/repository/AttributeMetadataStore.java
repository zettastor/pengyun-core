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

package py.monitor.customizable.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import py.monitor.exception.EmptyStoreException;
import py.monitor.jmx.server.ResourceType;

public interface AttributeMetadataStore {
  public void load() throws EmptyStoreException, Exception;

  public void add(AttributeMetadata attributeMetadata) throws Exception;

  public void append(Collection<AttributeMetadata> attributeMetadatas) throws Exception;

  public void delete(long attributeMetadataIndex) throws Exception;

  public void set(AttributeMetadata attributeMetadata) throws Exception;

  public boolean contains(AttributeMetadata attributeMetadata) throws Exception;

  public AttributeMetadata get(long attributeMetadataId) throws Exception;

  public Set<AttributeMetadata> get(List<Long> attributeMetadataIds) throws Exception;

  public AttributeMetadata get(String attributeMetadataName) throws Exception;

  public void saveOrUpdate(AttributeMetadata attributeMetadata) throws Exception;

  public Set<AttributeMetadata> getByServiceName(String serviceName) throws Exception;

  public Set<AttributeMetadata> getByResourceType(ResourceType resourceType) throws Exception;

  public Set<AttributeMetadata> getAll() throws Exception;

  public Set<AttributeMetadata> getAllByGroupName(String groupName) throws Exception;

  public Set<String> getServiceNameByAttributeId(long attributeId) throws Exception;

  public long size();

  public void commit() throws Exception;
}
