
package py.monitor.customizable.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.exception.EmptyStoreException;

public class AttributeStoreFactoryImpl implements AttributeStoreFactory {
  private static final Logger logger = LoggerFactory.getLogger(AttributeStoreFactoryImpl.class);

  @Override
  public AttributeMetadataStore create(Class<? extends AttributeMetadataStore> clazz)
      throws EmptyStoreException, Exception {
    logger.debug("{},{}", clazz.getName(), XmlStore.class.getName());

    if (clazz.getName().equals(XmlStore.class.getName())) {
      XmlStore xmlStore = new XmlStore();
      xmlStore.load();
      return xmlStore;
    } else {
      throw new Exception();
    }
  }

}
