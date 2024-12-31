

package py.metrics;

import com.codahale.metrics.Timer.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyTimerContextImpl implements PyTimerContext {
  private static final Logger logger = LoggerFactory.getLogger(PyTimerContextImpl.class);

  private final Context context;

  public PyTimerContextImpl(Context context) {
    if (context == null) {
      logger.error("given a null context !! {}", context, new Exception());
    }
    this.context = context;
  }

  @Override
  public long stop() {
    try {
      return context.stop();
    } catch (Throwable t) {
      logger.error("an error ?! {}", context, t);
      return -1;
    }
  }

}
