
package py.dd.common;

public enum ServicePort {
  InfoCenter(8020), DIH(10000), DriverContainer(9000), FsServer(8030), DataNode(10011), Console(
      8080), ScriptContainer(9090), MonitorCenter(11000), MonitorServer(11005), SystemDaemon(
      13333), RESTful(8081);

  private final int value;

  private ServicePort(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
