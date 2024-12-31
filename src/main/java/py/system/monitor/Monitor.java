
package py.system.monitor;

public interface Monitor {
  public String osName();

  public int currentPid();

  public ProcessInfo processInfo(int pid);

  public ProcessInfo[] processTable();

  public void killProcess(int pid);
}
