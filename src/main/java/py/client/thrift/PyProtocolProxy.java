/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

package py.client.thrift;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TTransport;

public class PyProtocolProxy extends TProtocol {
  private static final AtomicInteger sequenceId = new AtomicInteger(0);
  private TProtocol protocol;
  private TMessage msg;

  public PyProtocolProxy(TProtocol protocol) {
    super(null);
    this.protocol = protocol;
  }

  public String getMethodName() {
    return msg.name;
  }

  public int getSeqId() {
    return msg.seqid;
  }

  public TTransport getTransport() {
    return protocol.getTransport();
  }

  @Override
  public void writeMessageBegin(TMessage message) throws TException {
    msg = new TMessage(message.name, message.type, sequenceId.getAndIncrement());
    protocol.writeMessageBegin(msg);
  }

  @Override
  public void writeMessageEnd() throws TException {
    protocol.writeMessageEnd();
  }

  @Override
  public void writeStructBegin(TStruct struct) throws TException {
    protocol.writeStructBegin(struct);
  }

  @Override
  public void writeStructEnd() throws TException {
    protocol.writeStructEnd();
  }

  @Override
  public void writeFieldBegin(TField field) throws TException {
    protocol.writeFieldBegin(field);
  }

  @Override
  public void writeFieldEnd() throws TException {
    protocol.writeFieldEnd();
  }

  @Override
  public void writeFieldStop() throws TException {
    protocol.writeFieldStop();
  }

  @Override
  public void writeMapBegin(TMap map) throws TException {
    protocol.writeMapBegin(map);
  }

  @Override
  public void writeMapEnd() throws TException {
    protocol.writeMapEnd();
  }

  @Override
  public void writeListBegin(TList list) throws TException {
    protocol.writeListBegin(list);
  }

  @Override
  public void writeListEnd() throws TException {
    protocol.writeListEnd();
  }

  @Override
  public void writeSetBegin(TSet set) throws TException {
    protocol.writeSetBegin(set);
  }

  @Override
  public void writeSetEnd() throws TException {
    protocol.writeSetEnd();
  }

  @Override
  public void writeBool(boolean b) throws TException {
    protocol.writeBool(b);
  }

  @Override
  public void writeByte(byte b) throws TException {
    protocol.writeByte(b);
  }

  @Override
  public void writeI16(short i16) throws TException {
    protocol.writeI16(i16);
  }

  @Override
  public void writeI32(int i32) throws TException {
    protocol.writeI32(i32);
  }

  @Override
  public void writeI64(long i64) throws TException {
    protocol.writeI64(i64);
  }

  @Override
  public void writeDouble(double dub) throws TException {
    protocol.writeDouble(dub);
  }

  @Override
  public void writeString(String str) throws TException {
    protocol.writeString(str);
  }

  @Override
  public void writeBinary(ByteBuffer buf) throws TException {
    protocol.writeBinary(buf);
  }

  @Override
  public TMessage readMessageBegin() throws TException {
    TMessage message = protocol.readMessageBegin();
    return new TMessage(message.name, message.type, 0);
  }

  @Override
  public void readMessageEnd() throws TException {
    protocol.readMessageEnd();
  }

  @Override
  public TStruct readStructBegin() throws TException {
    return protocol.readStructBegin();
  }

  @Override
  public void readStructEnd() throws TException {
    protocol.readStructEnd();
  }

  @Override
  public TField readFieldBegin() throws TException {
    return protocol.readFieldBegin();
  }

  @Override
  public void readFieldEnd() throws TException {
    protocol.readFieldEnd();
  }

  @Override
  public TMap readMapBegin() throws TException {
    return protocol.readMapBegin();
  }

  @Override
  public void readMapEnd() throws TException {
    protocol.readMapEnd();
  }

  @Override
  public TList readListBegin() throws TException {
    return protocol.readListBegin();
  }

  @Override
  public void readListEnd() throws TException {
    protocol.readListEnd();
  }

  @Override
  public TSet readSetBegin() throws TException {
    return protocol.readSetBegin();
  }

  @Override
  public void readSetEnd() throws TException {
    protocol.readSetEnd();
  }

  @Override
  public boolean readBool() throws TException {
    return protocol.readBool();
  }

  @Override
  public byte readByte() throws TException {
    return protocol.readByte();
  }

  @Override
  public short readI16() throws TException {
    return protocol.readI16();
  }

  @Override
  public int readI32() throws TException {
    return protocol.readI32();
  }

  @Override
  public long readI64() throws TException {
    return protocol.readI64();
  }

  @Override
  public double readDouble() throws TException {
    return protocol.readDouble();
  }

  @Override
  public String readString() throws TException {
    return protocol.readString();
  }

  @Override
  public ByteBuffer readBinary() throws TException {
    ByteBuffer buffer = protocol.readBinary();
    return buffer;
  }

}
