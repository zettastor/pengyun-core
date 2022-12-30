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

package py.io.qos;

import java.sql.Blob;
import java.util.List;

public interface IoLimitationStore {
  public void update(IoLimitation ioLimitationInformation);

  public void save(IoLimitation ioLimitationInformation);

  public IoLimitation get(long ioLimitationId);

  public List<IoLimitation> list();

  public int delete(long ioLimitationId);

  public Blob createBlob(byte[] bytes);
}
