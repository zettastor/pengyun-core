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
