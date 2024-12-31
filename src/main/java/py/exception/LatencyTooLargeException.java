
package py.exception;

public class LatencyTooLargeException extends StorageException {
  public LatencyTooLargeException() {
    ioException = true;
  }
}

