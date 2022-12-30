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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.exception.EmptyStoreException;
import py.monitor.jmx.server.ResourceType;

public class DbStore implements AttributeMetadataStore {
  private static final Logger logger = LoggerFactory.getLogger(DbStore.class);
  private SessionFactory sessionFactory;

  @Override
  public void load() throws EmptyStoreException, Exception {
    throw new RuntimeException("Have not been implemented");
  }

  @Override
  public void add(AttributeMetadata attributeMetadata) throws Exception {
    sessionFactory.getCurrentSession().saveOrUpdate(attributeMetadata);
  }

  @Override
  public void append(Collection<AttributeMetadata> attributeMetadatas) throws Exception {
    throw new RuntimeException("Have not been implemented");
  }

  @Override
  public void delete(long attributeMetadataIndex) throws Exception {
    AttributeMetadata attributeMetadata = new AttributeMetadata();
    attributeMetadata.setId(attributeMetadataIndex);
    sessionFactory.getCurrentSession().delete(attributeMetadata);
  }

  @Override
  public void set(AttributeMetadata attributeMetadata) throws Exception {
    sessionFactory.getCurrentSession().saveOrUpdate(attributeMetadata);
  }

  @Override
  public boolean contains(AttributeMetadata attributeMetadata) throws Exception {
    try {
      this.get(attributeMetadata.getId());
      return true;
    } catch (Exception e) {
      logger.error("Caught an Exception", e);
      throw e;
    }
  }

  @Override
  public AttributeMetadata get(long attributeMetadataId) throws Exception {
    Query query = sessionFactory.getCurrentSession().createQuery("from attributes where id = :id");
    query.setLong("id", attributeMetadataId);
    AttributeMetadata attributeMetadata = (AttributeMetadata) query.uniqueResult();

    if (attributeMetadata != null && attributeMetadataId == attributeMetadata.getId()) {
      return attributeMetadata;
    } else {
      logger.error("Failed to get attribute from the database by id {}", attributeMetadataId);
      throw new Exception();
    }
  }

  @Override
  public Set<AttributeMetadata> get(List<Long> attributeMetadataIds) throws Exception {
    String condition = "(";
    for (Long attributeMetadataId : attributeMetadataIds) {
      condition += attributeMetadataId.toString();
      condition += ",";
    }

    condition = condition.substring(0, condition.length() - 1);
    condition += ")";
    logger.debug("Current condition string is {}", condition);

    Query query = sessionFactory.getCurrentSession()
        .createQuery("from attributes where id in :idSet");
    query.setString("idSet", condition);

    @SuppressWarnings("unchecked")
    List<AttributeMetadata> attributeMetadatas = query.list();

    if (attributeMetadatas != null) {
      Set<AttributeMetadata> returnData = new HashSet<AttributeMetadata>();
      for (AttributeMetadata attributeMetadata : attributeMetadatas) {
        returnData.add(attributeMetadata);
      }

      return returnData;
    } else {
      logger.error("Failed to get attribute from the database by id {}", condition);
      throw new Exception();
    }
  }

  @Override
  public AttributeMetadata get(String attributeMetadataName) throws Exception {
    Query query = sessionFactory.getCurrentSession()
        .createQuery("from attributes where attributeName = :attributeName");
    query.setString("attributeName", attributeMetadataName);
    AttributeMetadata attributeMetadata = (AttributeMetadata) query.uniqueResult();

    if (attributeMetadata != null && attributeMetadataName
        .equals(attributeMetadata.getAttributeName())) {
      return attributeMetadata;
    } else {
      logger.error("Failed to get attribute from the database by id {}", attributeMetadataName);
      throw new Exception();
    }
  }

  @Override
  public Set<AttributeMetadata> getByServiceName(String serviceName) throws Exception {
    Query query = sessionFactory.getCurrentSession()
        .createQuery("from attributes where serviceName = :serviceName");
    query.setString("serviceName", serviceName);

    @SuppressWarnings("unchecked")
    List<AttributeMetadata> attributeMetadatas = query.list();

    if (attributeMetadatas != null) {
      Set<AttributeMetadata> returnData = new HashSet<AttributeMetadata>();
      for (AttributeMetadata attributeMetadata : attributeMetadatas) {
        returnData.add(attributeMetadata);
      }

      return returnData;
    } else {
      logger.error("Failed to get attribute from the database by serviceName {}", serviceName);
      throw new Exception();
    }
  }

  @Override
  public Set<AttributeMetadata> getByResourceType(ResourceType resourceType) throws Exception {
    throw new RuntimeException("Not implemented!");
  }

  @Override
  public Set<AttributeMetadata> getAll() throws Exception {
    Query query = sessionFactory.getCurrentSession().createQuery("from attributes where 1 = 1");

    @SuppressWarnings("unchecked")
    List<AttributeMetadata> attributeMetadatas = query.list();

    if (attributeMetadatas != null) {
      Set<AttributeMetadata> returnData = new HashSet<AttributeMetadata>();
      for (AttributeMetadata attributeMetadata : attributeMetadatas) {
        returnData.add(attributeMetadata);
      }

      return returnData;
    } else {
      logger.error("Failed to get all attribute from database");
      throw new Exception();
    }
  }

  @Override
  public Set<AttributeMetadata> getAllByGroupName(String groupName) throws Exception {
    return null;
  }

  @Override
  public long size() {
    return 0;
  }

  @Override
  public void commit() throws Exception {
  }

  @Override
  public Set<String> getServiceNameByAttributeId(long attributeId) throws Exception {
    return null;
  }

  @Override
  public void saveOrUpdate(AttributeMetadata attributeMetadata) throws Exception {
  }

}
