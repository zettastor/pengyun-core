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

package py.common;

/**
 * This class is used by MutationLogFactory to manage mutation logs and reserved data buffers.
 * Please use this interface with caution.
 *
 * <p>It were preferable to use protected modifier to restrict its access, but it is not allow to
 * do so for an interface. Lame:(
 *
 */
public class HeapIndependentObject {
  boolean free = false;

  /**
   * Free this object.
   */
  public void free() {
    free = true;
  }

  /**
   * Allocate this object.
   */
  public void allocate() {
    free = false;
  }

  /**
   * check if the object has been allocated.
   *
   * @return true the object has been allocated. False the object has been freed
   */
  public boolean isAllocated() {
    return !free;
  }

  public void setPersisted() {
  }
}
