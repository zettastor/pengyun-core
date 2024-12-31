
package py.io.qos;

public enum MigrationStrategy {
  Smart(1), Manual(2);

  private int value;

  MigrationStrategy(int value) {
    this.value = value;
  }
}
