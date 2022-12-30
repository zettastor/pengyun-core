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

package py.informationcenter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface StoragePoolStore {
  public boolean saveStoragePool(StoragePool storagePool);

  public void deleteStoragePool(Long storagePoolId);

  public void removeArchiveFromStoragePool(Long storagePoolId, Long datanodeId, Long archiveId)
      throws SQLException, IOException;

  public StoragePool getStoragePool(Long storagePoolId) throws SQLException, IOException;

  public List<StoragePool> listAllStoragePools() throws SQLException, IOException;

  public List<StoragePool> listStoragePools(List<Long> storagePoolIds)
      throws SQLException, IOException;

  public List<StoragePool> listStoragePools(Long domainId) throws SQLException, IOException;

  public void clearMemoryMap();

  public void deleteVolumeId(Long storagePoolId, Long volumeId) throws SQLException, IOException;

  public void addVolumeId(Long storagePoolId, Long volumeId) throws SQLException, IOException;
}
