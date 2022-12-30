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

package py.engine.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import org.apache.commons.lang3.Validate;

public class PyEventPublisher {
  public static TranslatorOneArg TranslatorOneArg = new TranslatorOneArg();

  public static void publishEvent(PyDisruptor pyDisruptor, Long data) {
    Validate.notNull(pyDisruptor.getDisruptor());
    RingBuffer<PyEvent> ringBuffer = pyDisruptor.getDisruptor().getRingBuffer();
    ringBuffer.publishEvent(TranslatorOneArg, data);
  }

  static class TranslatorOneArg<T> implements EventTranslatorOneArg<PyEvent, T> {
    @Override
    public void translateTo(PyEvent event, long sequence, T data) {
      event.setData(data);
    }
  }
}
