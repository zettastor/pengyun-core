
package py.common;

public enum TraceAction {
  Write(1), Read(2), CommitLog(3), CleanTraceLog(4);

  private int value;

  TraceAction(int value) {
    this.value = value;
  }
}
