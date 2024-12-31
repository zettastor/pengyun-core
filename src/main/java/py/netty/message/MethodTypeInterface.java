

package py.netty.message;

public interface MethodTypeInterface {
  default boolean hasDefineMethod() {
    return true;
  }

  default boolean hasDefineDecodeMethod() {
    return true;
  }

  default boolean requestCarryData() {
    return false;
  }

  default boolean responseCarryData() {
    return false;
  }

  default boolean needReleaseMsgDataInRpc() {
    return true;
  }

  int getValue();

  String name();
}
