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
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

/**
 * A factory for creating a pair of protocols (one for input, one for output). Gives more
 * flexibility to the factory to enforce protocol restrictions (e.g. header protocol requires the
 * input/output transport/protocol are the same, a duplex header protocol factory can enforce the
 * transport restriction because it can see both transports, and can guarantee both protocols are
 * the same because it can fill in the input/output slots of the protocol pair with the same
 * protocol object).
 */
public abstract class TransDuplexProtocolFactory {
  public static TransDuplexProtocolFactory fromSingleFactory(
      final TProtocolFactory protocolFactory) {
    return new TransDuplexProtocolFactory() {
      @Override
      public ThriftProtocolPair getProtocolPair(ThriftTransportPair transportPair) {
        return ThriftProtocolPair.fromSeparateProtocols(
            protocolFactory.getProtocol(transportPair.getInputTransport()),
            protocolFactory.getProtocol(transportPair.getOutputTransport()));
      }
    };
  }

  public static TransDuplexProtocolFactory fromSeparateFactories(
      final TProtocolFactory inputProtocolFactory,
      final TProtocolFactory outputProtocolFactory) {
    return new TransDuplexProtocolFactory() {
      @Override
      public ThriftProtocolPair getProtocolPair(ThriftTransportPair transportPair) {
        return ThriftProtocolPair.fromSeparateProtocols(
            inputProtocolFactory.getProtocol(transportPair.getInputTransport()),
            outputProtocolFactory.getProtocol(transportPair.getOutputTransport()));
      }
    };
  }

  public abstract ThriftProtocolPair getProtocolPair(ThriftTransportPair transportPair);

  public TProtocolFactory getInputProtocolFactory() {
    return new TProtocolFactory() {
      @Override
      public TProtocol getProtocol(TTransport trans) {
        return getProtocolPair(ThriftTransportPair.fromSingleTransport(trans)).getInputProtocol();
      }
    };
  }

  public TProtocolFactory getOutputProtocolFactory() {
    return new TProtocolFactory() {
      @Override
      public TProtocol getProtocol(TTransport trans) {
        return getProtocolPair(ThriftTransportPair.fromSingleTransport(trans)).getOutputProtocol();
      }
    };
  }
}
