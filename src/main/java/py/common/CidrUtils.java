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

package py.common;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that enables to get an IP range from CIDR specification. It supports both IPv4 and IPv6.
 */
public class CidrUtils {
  private final String cidr;
  private final int prefixLength;
  private InetAddress inetAddress;
  private InetAddress startAddress;
  private InetAddress endAddress;

  public CidrUtils(String cidr) throws UnknownHostException {
    this.cidr = cidr;

    /* split CIDR to address and prefix part */
    if (this.cidr.contains("/")) {
      int index = this.cidr.indexOf("/");
      String addressPart = this.cidr.substring(0, index);
      String networkPart = this.cidr.substring(index + 1);

      inetAddress = InetAddress.getByName(addressPart);
      prefixLength = Integer.parseInt(networkPart);

      calculate();
    } else {
      throw new IllegalArgumentException("not an valid CIDR format!");
    }
  }

  private void calculate() throws UnknownHostException {
    ByteBuffer maskBuffer;
    int targetSize;
    if (inetAddress.getAddress().length == 4) {
      maskBuffer =
          ByteBuffer
              .allocate(4)
              .putInt(-1);
      targetSize = 4;
    } else {
      maskBuffer = ByteBuffer.allocate(16)
          .putLong(-1L)
          .putLong(-1L);
      targetSize = 16;
    }

    BigInteger mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefixLength);

    ByteBuffer buffer = ByteBuffer.wrap(inetAddress.getAddress());
    BigInteger ipVal = new BigInteger(1, buffer.array());

    BigInteger startIp = ipVal.and(mask);
    BigInteger endIp = startIp.add(mask.not());

    byte[] startIpArr = toBytes(startIp.toByteArray(), targetSize);
    byte[] endIpArr = toBytes(endIp.toByteArray(), targetSize);

    this.startAddress = InetAddress.getByAddress(startIpArr);
    this.endAddress = InetAddress.getByAddress(endIpArr);

  }

  private byte[] toBytes(byte[] array, int targetSize) {
    int counter = 0;
    List<Byte> newArr = new ArrayList<Byte>();
    while (counter < targetSize && (array.length - 1 - counter >= 0)) {
      newArr.add(0, array[array.length - 1 - counter]);
      counter++;
    }

    int size = newArr.size();
    for (int i = 0; i < (targetSize - size); i++) {
      newArr.add(0, (byte) 0);
    }

    byte[] ret = new byte[newArr.size()];
    for (int i = 0; i < newArr.size(); i++) {
      ret[i] = newArr.get(i);
    }
    return ret;
  }

  public String getNetworkAddress() {
    return this.startAddress.getHostAddress();
  }

  public String getBroadcastAddress() {
    return this.endAddress.getHostAddress();
  }

  public boolean isInRange(String ipAddress) throws UnknownHostException {
    InetAddress address = InetAddress.getByName(ipAddress);
    BigInteger start = new BigInteger(1, this.startAddress.getAddress());
    BigInteger end = new BigInteger(1, this.endAddress.getAddress());
    BigInteger target = new BigInteger(1, address.getAddress());

    int st = start.compareTo(target);
    int te = target.compareTo(end);

    return (st == -1 || st == 0) && (te == -1 || te == 0);
  }
}