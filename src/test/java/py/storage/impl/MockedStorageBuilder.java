

package py.storage.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import py.storage.Storage;

public class MockedStorageBuilder {
  public static Storage build(String id, long size) {
    Storage storage = mock(Storage.class);
    when(storage.size()).thenReturn(size);
    when(storage.identifier()).thenReturn(id);
    return storage;
  }

  public static List<Storage> build(int numStorages, long size) {
    List<Storage> storages = new ArrayList<>();
    for (int i = 0; i < numStorages; i++) {
      storages.add(build("DummyStorage-" + i + "(size:" + size + ")", size));
    }
    return storages;
  }
}
