/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package py.common.rpc.share;

import org.apache.thrift.protocol.TProtocol;

/**
 * Represents a pair of protocols: one for input and one for output.
 */
public class ThriftProtocolPair {
  private final TProtocol inputProtocol;
  private final TProtocol outputProtocol;

  protected ThriftProtocolPair(TProtocol inputProtocol, TProtocol outputProtocol) {
    this.inputProtocol = inputProtocol;
    this.outputProtocol = outputProtocol;
  }

  public static ThriftProtocolPair fromSeparateProtocols(final TProtocol inputProtocol,
      final TProtocol outputProtocol) {
    return new ThriftProtocolPair(inputProtocol, outputProtocol);
  }

  public static ThriftProtocolPair fromSingleProtocol(final TProtocol protocol) {
    return new ThriftProtocolPair(protocol, protocol);
  }

  public TProtocol getInputProtocol() {
    return inputProtocol;
  }

  public TProtocol getOutputProtocol() {
    return outputProtocol;
  }
}
