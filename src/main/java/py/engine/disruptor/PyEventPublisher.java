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
