
package py.app.thrift;

import org.apache.thrift.TProcessor;

/**
 * Thrift doesn't have an interface of processor factory.
 */
public interface ThriftProcessorFactory {
  public TProcessor getProcessor();
}
