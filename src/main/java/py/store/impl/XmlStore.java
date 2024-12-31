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

package py.store.impl;

import java.io.File;
import java.io.FileReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.exception.EmptyException;
import py.monitor.exception.EmptyStoreException;
import py.store.IntCommitter;
import py.store.IntLoader;

public class XmlStore<DataT> {
  private static final Logger logger = LoggerFactory.getLogger(XmlStore.class);
  protected DataT data;
  private Class<DataT> clazz;

  public IntLoader<String> load(Class<DataT> clazz) throws EmptyException, Exception {
    this.clazz = clazz;
    try {
      return new Loader();
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      throw e;
    }
  }

  public IntCommitter<String, DataT> commit(DataT data) throws Exception {
    this.data = data;
    return new Committer();
  }

  public class Loader implements IntLoader<String> {
    @Override
    @SuppressWarnings("unchecked")
    public void from(String path) throws EmptyStoreException, Exception {
      logger.debug("File path : {}", path);

      File xmlFile = new File(path);
      if (!xmlFile.exists()) {
        xmlFile.createNewFile();
        throw new EmptyStoreException();
      } else {
        if (xmlFile.getTotalSpace() == 0L) {
          throw new EmptyStoreException();
        } else {
          FileReader fileReader = new FileReader(xmlFile);
          JAXBContext context = JAXBContext.newInstance(clazz);
          Unmarshaller um = context.createUnmarshaller();
          data = (DataT) um.unmarshal(fileReader);
        }
      }
    }

  }

  public class Committer implements IntCommitter<String, DataT> {
    private Class<DataT> clazz;

    @Override
    public void to(String path) throws Exception {
      logger.debug("Going to write data to disk, file path is : {}", path);

      JAXBContext context = JAXBContext.newInstance(this.clazz);
      Marshaller mmarshaller = context.createMarshaller();
      mmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      mmarshaller.marshal(data, new File(path));
    }

    @Override
    public IntCommitter<String, DataT> inFormatOf(Class<DataT> clazz) throws Exception {
      this.clazz = clazz;
      return this;
    }

  }

}
