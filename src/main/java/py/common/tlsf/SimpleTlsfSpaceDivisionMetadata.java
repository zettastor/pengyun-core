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

package py.common.tlsf;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTlsfSpaceDivisionMetadata implements TlsfSpaceDivisionMetadata {
  private static final Logger logger = LoggerFactory
      .getLogger(SimpleTlsfSpaceDivisionMetadata.class);
  protected final Map<Long, DivisionMetadata> metadataTable = new HashMap<>();

  @Override
  public boolean seperated() {
    return false;
  }

  @Override
  public long getPrePhysicalAddress(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    return metadata.prePhyAddr;
  }

  @Override
  public void setPrePhysicalAddress(long address, long prePhysicalAddress) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }

    metadata.prePhyAddr = prePhysicalAddress;
  }

  @Override
  public long getNextPhysicalAddress(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    long nextPhyAddr = metadata.curPhyAddr + TlsfSpaceDivisionMetadata.ASSEMEM_OFFSET
        + getAccessibleMemSize(address) - TlsfSpaceDivisionMetadata.METADATA_UNIT_BYTES;
    return nextPhyAddr;
  }

  @Override
  public long getAddress(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    return metadata.curPhyAddr;
  }

  @Override
  public void setAddress(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }

    metadata.curPhyAddr = address;
  }

  @Override
  public long getAccessibleMemSize(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    return metadata.sizeField & ~(BUFFER_FREE_BIT | PRE_BUFFER_FREE_BIT);
  }

  @Override
  public void setAccessibleMemSize(long address, long accessibleMemSize) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }
    metadata.sizeField =
        accessibleMemSize | (metadata.sizeField & (BUFFER_FREE_BIT | PRE_BUFFER_FREE_BIT));
  }

  @Override
  public long getPreFreeAddress(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    return metadata.preFreeAddr;
  }

  @Override
  public void setPreFreeAddress(long address, long preFreeAddress) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }
    metadata.preFreeAddr = preFreeAddress;
  }

  @Override
  public long getNextFreeAddress(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    return metadata.nextFreeAddr;
  }

  @Override
  public void setNextFreeAddress(long address, long nextFreeAddress) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }

    metadata.nextFreeAddr = nextFreeAddress;
  }

  @Override
  public boolean isFree(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    return (metadata.sizeField & BUFFER_FREE_BIT) != 0;
  }

  @Override
  public void setFree(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }
    metadata.sizeField |= BUFFER_FREE_BIT;
  }

  @Override
  public boolean isPreFree(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    return (metadata.sizeField & PRE_BUFFER_FREE_BIT) != 0;
  }

  @Override
  public void setPreFree(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }
    metadata.sizeField |= PRE_BUFFER_FREE_BIT;
  }

  @Override
  public void setUsed(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }
    metadata.sizeField &= ~BUFFER_FREE_BIT;
  }

  @Override
  public void setPreUsed(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    if (metadata == null) {
      metadata = new DivisionMetadata();
      metadataTable.put(address, metadata);
    }
    metadata.sizeField &= ~PRE_BUFFER_FREE_BIT;
  }

  public void clear(long address) {
    metadataTable.remove(address);
  }

  protected static class DivisionMetadata {
    public long prePhyAddr = TlsfSpaceManager.NULL_SPACE_ADDRESS;
    public long curPhyAddr = TlsfSpaceManager.NULL_SPACE_ADDRESS;
    public long sizeField = 0L;
    public long nextFreeAddr = TlsfSpaceManager.NULL_SPACE_ADDRESS;
    public long preFreeAddr = TlsfSpaceManager.NULL_SPACE_ADDRESS;
  }

}
