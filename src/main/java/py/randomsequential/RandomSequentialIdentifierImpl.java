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

package py.randomsequential;

public class RandomSequentialIdentifierImpl implements RandomSequentialIdentifier {
  private final IoStateMachine ioStateMachine;

  public RandomSequentialIdentifierImpl(int sequentialConditionThreshold,
      int randomConditionThreshold) {
    ioStateMachine = new IoStateMachine(sequentialConditionThreshold, randomConditionThreshold);
  }

  @Override
  public synchronized boolean updateLastOffsetAndIsSequential(long offset, int length) {
    IoStateMachine.State state = ioStateMachine.process(offset, length);

    if (state == IoStateMachine.State.IO_RANDOM) {
      return false;
    } else {
      return true;
    }
  }

}
