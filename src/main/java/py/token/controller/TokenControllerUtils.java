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

package py.token.controller;

import java.util.concurrent.TimeUnit;

public class TokenControllerUtils {
  public static TokenController generateAndRegister(int bucketCapacity) {
    TokenController controller = TokenControllerFactory.getInstance().create(bucketCapacity);
    TokenControllerCenter.getInstance().register(controller);
    return controller;
  }

  public static void deregister(TokenController controller) {
    TokenControllerCenter.getInstance().deregister(controller);
  }

  public static TokenController generateBogusController() {
    return new TokenController() {
      @Override
      public long getId() {
        return 0;
      }

      @Override
      public void reset() {
      }

      @Override
      public boolean acquireToken(long timeout, TimeUnit timeUnit) {
        return true;
      }

      @Override
      public boolean acquireToken(int tokenCount, long timeout, TimeUnit timeUnit) {
        return true;
      }

      @Override
      public boolean acquireToken() {
        return true;
      }

      @Override
      public boolean acquireToken(int tokenCount) {
        return true;
      }

      @Override
      public int tryAcquireToken(int tokenCount) {
        return tokenCount;
      }

      @Override
      public void updateToken(int bucketCapacity) {
      }

    };
  }
}
