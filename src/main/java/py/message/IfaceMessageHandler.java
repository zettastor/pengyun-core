

package py.message;

public interface IfaceMessageHandler {
  public void execute(InterfaceMessage<?> message) throws Exception;
}
