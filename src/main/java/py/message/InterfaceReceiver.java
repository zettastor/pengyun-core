
package py.message;

import java.util.Map;
import py.message.exceptions.MessageMapMissingException;

public interface InterfaceReceiver {
  public void buildMessageMap() throws Exception;

  public Map<Class<?>, IfaceMessageHandler> getMessageMap() throws MessageMapMissingException;

  public void regist();

  public void unregist();
}
