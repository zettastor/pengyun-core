

package py.netty.core;

import com.google.protobuf.AbstractMessageLite;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Method;
import py.netty.exception.AbstractNettyException;
import py.netty.exception.InvalidProtocolException;
import py.netty.exception.NotSupportedException;
import py.netty.message.Header;
import py.netty.message.Message;

public interface Protocol {
  public ByteBuf encodeRequest(Header header, AbstractMessageLite metadata)
      throws InvalidProtocolException;

  public ByteBuf encodeResponse(Header header, AbstractMessageLite metadata)
      throws InvalidProtocolException;

  public Object decodeRequest(Message msg) throws InvalidProtocolException;

  public Object decodeResponse(Message msg) throws InvalidProtocolException;

  public AbstractNettyException decodeException(ByteBuf buffer) throws InvalidProtocolException;

  public ByteBuf encodeException(long requestId, AbstractNettyException e);

  public Method getMethod(int methodType) throws NotSupportedException;
}
