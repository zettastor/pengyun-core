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

package py.common.tlsf.bytebuffer.manager;

import py.common.tlsf.SimpleTlsfSpaceDivisionMetadata;
import py.common.tlsf.TlsfSpaceDivisionMetadata;

/**
 * An implementation for direct byte buffer manager of interface {@link TlsfSpaceDivisionMetadata}.
 *
 */
public class ByteBufferDivisionMetadata extends SimpleTlsfSpaceDivisionMetadata {
  @Override
  public boolean seperated() {
    // the metadata of tlsf division is seperated from data in space
    return true;
  }

  @Override
  public long getNextPhysicalAddress(long address) {
    DivisionMetadata metadata = metadataTable.get(address);
    long nextPhyAddr = metadata.curPhyAddr + getAccessibleMemSize(address);
    return nextPhyAddr;
  }
}
