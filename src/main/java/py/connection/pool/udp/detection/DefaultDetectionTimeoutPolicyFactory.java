
package py.connection.pool.udp.detection;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultDetectionTimeoutPolicyFactory implements DetectionTimeoutPolicyFactory {
  private final int maxRetryTimes;
  private final long initTimeoutMs;

  public DefaultDetectionTimeoutPolicyFactory(int maxRetryTimes, long initTimeoutMs) {
    this.maxRetryTimes = maxRetryTimes;
    this.initTimeoutMs = initTimeoutMs;
  }

  @Override
  public DetectionTimeoutPolicy generate() {
    return new DetectionTimeoutPolicy() {
      AtomicInteger currentRetryTimes = new AtomicInteger(1);

      @Override
      public long getTimeoutMs() {
        int time = currentRetryTimes.getAndIncrement();

        if (time > maxRetryTimes) {
          return -1;
        }

        return initTimeoutMs * time;
      }
    };
  }

}
