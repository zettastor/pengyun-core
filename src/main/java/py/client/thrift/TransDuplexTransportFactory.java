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

package py.client.thrift;

import org.apache.thrift.transport.TTransportFactory;
import py.common.rpc.share.ThriftTransportPair;
import py.common.rpc.share.TransDuplexProtocolFactory;

public abstract class TransDuplexTransportFactory {
  public static TransDuplexTransportFactory fromSingleTransportFactory(
      final TTransportFactory transportFactory
  ) {
    return new TransDuplexTransportFactory() {
      @Override
      public ThriftTransportPair getTransportPair(ThriftTransportPair transportPair) {
        return ThriftTransportPair.fromSeparateTransports(
            transportFactory.getTransport(transportPair.getInputTransport()),
            transportFactory.getTransport(transportPair.getOutputTransport()));
      }
    };
  }

  public static TransDuplexTransportFactory fromSeparateTransportFactories(
      final TTransportFactory inputTransportFactory,
      final TTransportFactory outputTransportFactory
  ) {
    return new TransDuplexTransportFactory() {
      @Override
      public ThriftTransportPair getTransportPair(ThriftTransportPair transportPair) {
        return ThriftTransportPair.fromSeparateTransports(
            inputTransportFactory.getTransport(transportPair.getInputTransport()),
            outputTransportFactory.getTransport(transportPair.getOutputTransport())
        );
      }
    };
  }

  public abstract ThriftTransportPair getTransportPair(ThriftTransportPair transportPair);
}
