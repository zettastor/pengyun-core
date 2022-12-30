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

import org.apache.thrift.transport.TTransport;

/**
 * Represents a pair of transports: one for input and one for output.
 */
public class ThriftTransportPair {
  private final TTransport inputTransport;
  private final TTransport outputTransport;

  protected ThriftTransportPair(TTransport inputTransport, TTransport outputTransport) {
    this.inputTransport = inputTransport;
    this.outputTransport = outputTransport;
  }

  public static ThriftTransportPair fromSeparateTransports(final TTransport inputTransport,
      final TTransport outputTransport) {
    return new ThriftTransportPair(inputTransport, outputTransport);
  }

  public static ThriftTransportPair fromSingleTransport(final TTransport transport) {
    return new ThriftTransportPair(transport, transport);
  }

  public TTransport getInputTransport() {
    return inputTransport;
  }

  public TTransport getOutputTransport() {
    return outputTransport;
  }
}
