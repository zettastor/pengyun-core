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

package py.common.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IdUtils {
  public static List<Long> toLongs(Collection<? extends AbstractId> ids) {
    List<Long> longIds = new ArrayList<Long>();
    if (ids != null) {
      for (AbstractId id : ids) {
        longIds.add(id.getId());
      }
    }
    return longIds;
  }
}
