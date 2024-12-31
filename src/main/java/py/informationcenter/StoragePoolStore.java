

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
