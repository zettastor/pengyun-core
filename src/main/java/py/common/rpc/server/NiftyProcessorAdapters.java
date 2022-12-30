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

package py.common.rpc.server;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import org.apache.thrift.ProcessFunction;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBaseProcessor;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiftyProcessorAdapters {
  private static final Logger logger = LoggerFactory.getLogger(NiftyProcessorAdapters.class);
  private static Field ifaceFiled;

  private static String metricPrefix = "Server_process";

  /**
   * Adapt a {@link TProcessor} to a standard Thrift {@link NiftyProcessor}. Nifty uses this
   * internally to adapt the processors generated by the standard Thrift code generator into
   * instances of {@link NiftyProcessor} usable by {@link com.facebook.nifty.core.NiftyDispatcher}
   */
  public static NiftyProcessor processorFromThriftProcessor(
      final TProcessor standardThriftProcessor) {
    return new NiftyProcessor() {
      @Override
      public ListenableFuture<Boolean> process(TProtocol in, TProtocol out,
          RequestContext requestContext)
          throws TException {
        /*
         * for catching all exceptions include which are not derived from TException,
         * we implements our the function of process().
         */
        if (ifaceFiled == null) {
          try {
            ifaceFiled = TBaseProcessor.class.getDeclaredField("iface");
            ifaceFiled.setAccessible(true);
          } catch (NoSuchFieldException | SecurityException e) {
            throw new TException("can not get iface from processor " + e);
          }
        }

        TMessage msg = null;
        ProcessFunction fn = null;

        if (standardThriftProcessor instanceof TBaseProcessor) {
          msg = in.readMessageBegin();
          TBaseProcessor process = (TBaseProcessor) standardThriftProcessor;

          // recv a request, map call method here
          fn = (ProcessFunction) process.getProcessMapView().get(msg.name);
        } else {
          logger.error("processor is not TBaseProcessor");
          throw new TException("processor is not TBaseProcessor");
        }

        try {
          if (fn == null) {
            TProtocolUtil.skip(in, TType.STRUCT);
            in.readMessageEnd();
            TApplicationException x = new TApplicationException(
                TApplicationException.UNKNOWN_METHOD,
                "Invalid method name: '" + msg.name + "'");
            out.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
            x.write(out);
            out.writeMessageEnd();
            out.getTransport().flush();
          } else {
            // server recv a request here, start to record metrics
            // now how to get the method name
            String methodName = fn.getMethodName();
            try {
              fn.process(msg.seqid, in, out, ifaceFiled.get(standardThriftProcessor));
            } finally {
              logger.info("nothing need to do here");
            }
          }

        } catch (Throwable t) {
          logger.error("caught an exception", t);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR,
              "Internal error processing " + msg.name);
          out.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
          x.write(out);
          out.writeMessageEnd();
          out.getTransport().flush();
        }

        return Futures.immediateFuture(true);
      }
    };
  }

  /**
   * Create a {@link NiftyProcessorFactory} that always returns the same {@link NiftyProcessor}
   * adapted from the given standard Thrift {@link TProcessor}.
   */
  public static NiftyProcessorFactory factoryFromThriftProcessor(
      final TProcessor standardThriftProcessor) {
    checkProcessMethodSignature();

    return new NiftyProcessorFactory() {
      @Override
      public NiftyProcessor getProcessor(TTransport transport) {
        return processorFromThriftProcessor(standardThriftProcessor);
      }
    };
  }

  /**
   * Create a {@link NiftyProcessorFactory} that delegates to a standard Thrift {@link
   * TProcessorFactory} to construct an instance, then adapts each instance to a {@link
   * NiftyProcessor}.
   */
  public static NiftyProcessorFactory factoryFromThriftProcessorFactory(
      final TProcessorFactory standardThriftProcessorFactory) {
    checkProcessMethodSignature();

    return new NiftyProcessorFactory() {
      @Override
      public NiftyProcessor getProcessor(TTransport transport) {
        return processorFromThriftProcessor(standardThriftProcessorFactory.getProcessor(transport));
      }
    };
  }

  /**
   * Adapt a {@link NiftyProcessor} to a standard Thrift {@link TProcessor}. The {@link
   * com.facebook.nifty.core.NiftyRequestContext} will always be {@code null}
   */
  public static TProcessor processorToThriftProcessor(final NiftyProcessor niftyProcessor) {
    return new TProcessor() {
      @Override
      public boolean process(TProtocol in, TProtocol out) throws TException {
        try {
          return niftyProcessor.process(in, out, null).get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new TException(e);
        } catch (ExecutionException e) {
          throw new TException(e);
        }
      }
    };
  }

  /**
   * Create a standard thrift {@link TProcessorFactory} that always returns the same {@link
   * TProcessor} adapted from the given {@link NiftyProcessor}.
   */
  public static TProcessorFactory processorToThriftProcessorFactory(
      final NiftyProcessor niftyProcessor) {
    return new TProcessorFactory(processorToThriftProcessor(niftyProcessor));
  }

  /**
   * Create a standard thrift {@link TProcessorFactory} that delegates to a {@link
   * NiftyProcessorFactory} to construct an instance, then adapts each instance to a standard Thrift
   * {@link TProcessor}.
   */
  public static TProcessorFactory processorFactoryToThriftProcessorFactory(
      final NiftyProcessorFactory niftyProcessorFactory) {
    return new TProcessorFactory(null) {
      @Override
      public TProcessor getProcessor(TTransport trans) {
        return processorToThriftProcessor(niftyProcessorFactory.getProcessor(trans));
      }
    };
  }

  /**
   * Catch the mismatch early if someone tries to pass our internal variant of TProcessor with a
   * different signature on the process() method into these adapters.
   */
  private static void checkProcessMethodSignature() {
    try {
      TProcessor.class.getMethod("process", TProtocol.class, TProtocol.class);
    } catch (NoSuchMethodException e) {
      // Facebook's TProcessor variant needs processor adapters from a different package
      throw new IllegalStateException(
          "The loaded TProcessor class is not supported by version of the adapters");
    }
  }
}
