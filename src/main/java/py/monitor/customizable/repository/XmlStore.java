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

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.PyService;
import py.exception.NotFoundException;
import py.monitor.exception.EmptyStoreException;
import py.monitor.jmx.server.ResourceType;

@XmlRootElement(namespace = "py.monitor.aop.pojo.management.repository.XmlStore")
@XmlType(propOrder = {"name", "attributeMetadataList"})
@XmlAccessorType(XmlAccessType.NONE)
public class XmlStore implements AttributeMetadataStore {
  private static final Logger logger = LoggerFactory.getLogger(XmlStore.class);
  private static final String DEFAULT_JMX_MBEAN_ATTRIBUTE_XML = System.getProperty("user.dir")
      + "/config/AttributeRepository.xml";
  private String path;

  @XmlElement(name = "items")
  private Set<AttributeMetadata> attributeMetadataList;

  @XmlElement(name = "name")
  private String name;

  public XmlStore() {
    this.path = DEFAULT_JMX_MBEAN_ATTRIBUTE_XML;
    attributeMetadataList = new HashSet<AttributeMetadata>();
  }

  public XmlStore(String path) {
    this.path = path;
    attributeMetadataList = new HashSet<AttributeMetadata>();
  }

  public Set<AttributeMetadata> getAttributeMetadataList() {
    return attributeMetadataList;
  }

  public void setAttributeMetadataList(Set<AttributeMetadata> attributeMetadataList) {
    this.attributeMetadataList = attributeMetadataList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void load() throws EmptyStoreException, Exception {
    logger.debug("Attribute repository xml file path : {}", path);

    File xmlFile = new File(path);
    if (!xmlFile.exists()) {
      throw new EmptyStoreException();
    }

    if (xmlFile.getTotalSpace() == 0L) {
      throw new EmptyStoreException();
    } else {
      FileReader fileReader = new FileReader(xmlFile);
      JAXBContext context = JAXBContext.newInstance(XmlStore.class);
      Unmarshaller um = context.createUnmarshaller();
      XmlStore xmlStore = (XmlStore) um.unmarshal(fileReader);
      logger.debug("Repository store is {}", xmlStore);
      this.setName(xmlStore.getName());
      this.setAttributeMetadataList(xmlStore.getAttributeMetadataList());
    }
  }

  @Override
  public void add(AttributeMetadata attributeMetadata) throws Exception {
    logger.debug("Going to add new attribute metadata to store : {}", attributeMetadata);
    logger.debug("Service group not existed, going to create a new one and save new attribute");

    for (String belongsTo : attributeMetadata.getServices()) {
      if (!PyService.is(belongsTo).legal()) {
        logger.error("Invailid service name : {}", belongsTo);
        throw new Exception();
      }
    }

    attributeMetadataList.add(attributeMetadata);
    logger.debug("Attibute has already save to the store");
  }

  @Override
  public void append(Collection<AttributeMetadata> attributeMetadatas) throws Exception {
    for (AttributeMetadata attributeMetadata : attributeMetadatas) {
      this.add(attributeMetadata);
    }
  }

  @Override
  public void delete(long attributeMetadataIndex) throws Exception {
    AttributeMetadata attributeMetadata = new AttributeMetadata();
    attributeMetadata.setId(attributeMetadataIndex);
    attributeMetadataList.remove(attributeMetadata);
  }

  @Override
  public void set(AttributeMetadata attributeMetadata) throws Exception {
  }

  @Override
  public AttributeMetadata get(long attributeMetadataId) throws Exception {
    for (AttributeMetadata attribute : attributeMetadataList) {
      if (attribute.getId() == attributeMetadataId) {
        return attribute;
      }
    }

    throw new Exception("The attribute that you want to get is not existed right now");
  }

  @Override
  public Set<AttributeMetadata> get(List<Long> attributeMetadataIds) throws Exception {
    Set<AttributeMetadata> returnObjects = new HashSet<AttributeMetadata>();
    for (AttributeMetadata attribute : attributeMetadataList) {
      if (attributeMetadataIds.contains(attribute.getId())) {
        returnObjects.add(attribute);
        break;
      }
    }

    return returnObjects;
  }

  @Override
  public AttributeMetadata get(String attributeMetadataName) throws Exception {
    for (AttributeMetadata attribute : attributeMetadataList) {
      if (attribute.getAttributeName().equals(attributeMetadataName)) {
        return attribute;
      }
    }

    throw new Exception("The attribute that you want to get is not existed right now");
  }

  @Override
  public void saveOrUpdate(AttributeMetadata attributeMetadata) throws Exception {
    for (String belongsTo : attributeMetadata.getServices()) {
      if (!PyService.is(belongsTo).legal()) {
        logger.error("Invailid service name : {}", belongsTo);
        throw new Exception();
      }
    }

    if (!contains(attributeMetadata)) {
      logger
          .debug("Going to add attribute metadata : <<{}>> to attribute store.", attributeMetadata);
      this.attributeMetadataList.add(attributeMetadata);
    } else {
      for (String serviceName : attributeMetadata.getServices()) {
        if (!attributeMetadata.getServices().contains(serviceName)) {
          attributeMetadata.getServices().add(serviceName);
        }
      }
    }
  }

  @Override
  public Set<AttributeMetadata> getAll() throws Exception {
    Set<AttributeMetadata> attributes = new HashSet<AttributeMetadata>();
    for (AttributeMetadata attribute : attributeMetadataList) {
      attributes.add(attribute);
    }
    return attributes;
  }

  @Override
  public Set<AttributeMetadata> getAllByGroupName(String groupName) throws Exception {
    Set<AttributeMetadata> attributes = new HashSet<AttributeMetadata>();
    for (AttributeMetadata attribute : attributeMetadataList) {
      if (attribute.getServices().contains(groupName)) {
        attributes.add(attribute);
      }
    }
    return attributes;
  }

  @Override
  public void commit() throws Exception {
    logger.debug("Going to write data to disk, file path is : {}", path);

    JAXBContext context = JAXBContext.newInstance(XmlStore.class);
    Marshaller mmarshaller = context.createMarshaller();
    mmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    mmarshaller.marshal(this, new File(path));
  }

  @Override
  public boolean contains(AttributeMetadata attributeMetadata) throws Exception {
    return this.attributeMetadataList.contains(attributeMetadata);
  }

  @Override
  public Set<AttributeMetadata> getByServiceName(String serviceName) throws Exception {
    Set<AttributeMetadata> attributeMetadatas = new HashSet<AttributeMetadata>();
    for (AttributeMetadata attributeMetadata : attributeMetadataList) {
      if (attributeMetadata.getServices().contains(serviceName)) {
        attributeMetadatas.add(attributeMetadata);
        break;
      }
    }

    return attributeMetadatas;
  }

  @Override
  public Set<AttributeMetadata> getByResourceType(ResourceType resourceType) throws Exception {
    Set<AttributeMetadata> attributes = new HashSet<AttributeMetadata>();
    for (AttributeMetadata attributeMetadata : attributeMetadataList) {
      if (attributeMetadata.getResourceType().equals(resourceType)) {
        attributes.add(attributeMetadata);
      }
    }

    return attributes;
  }

  @Override
  public Set<String> getServiceNameByAttributeId(long attributeId) throws Exception {
    for (AttributeMetadata attributeMetadata : attributeMetadataList) {
      if (attributeMetadata.getId() == attributeId) {
        return attributeMetadata.getServices();
      }
    }

    logger.error("There is no attribute with the id: {}", attributeId);
    throw new NotFoundException();
  }

  @Override
  public long size() {
    return this.attributeMetadataList.size();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((attributeMetadataList == null) ? 0 : attributeMetadataList.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    XmlStore other = (XmlStore) obj;
    if (attributeMetadataList == null) {
      if (other.attributeMetadataList != null) {
        return false;
      }
    } else if (!attributeMetadataList.equals(other.attributeMetadataList)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XMLStore [attributeMetadataList=" + attributeMetadataList + ", name=" + name + "]";
  }

}
