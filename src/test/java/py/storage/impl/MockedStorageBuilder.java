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
