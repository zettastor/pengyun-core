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
