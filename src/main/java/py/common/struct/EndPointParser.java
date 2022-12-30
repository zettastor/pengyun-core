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

package py.common.struct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.CidrUtils;
import py.common.OsCmdExecutor;
import py.common.OsCmdExecutor.OsCmdOutputLogger;
import py.common.OsCmdExecutor.OsCmdStreamConsumer;

public class EndPointParser {
  public static final int MIN_PORT = 1;
  public static final int MAX_PORT = 65535;
  public static final int IPV4_MAX_MASK_LEN = 32;
  public static final String IPV4_PATTERN = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";

  public static final Set<String> MAIN_IPS;
  private static final Logger logger = LoggerFactory.getLogger(EndPointParser.class);
  private static final Pattern addressPattern = Pattern.compile(IPV4_PATTERN);

  static {
    MAIN_IPS = mainIps();
  }

  public static EndPoint parseLocalEndPoint(int port, String localIp) {
    validatePort(port);
    return new EndPoint(localIp, port);
  }

  public static EndPoint parseLocalEndPoint(String endpoint, String localIp) {
    Validate.notNull(endpoint, "Endpoint to be parsed cannot be null");
    Validate.notEmpty(endpoint, "Endpoint to be parsed cannot be empty");

    String[] parts = endpoint.split(":");
    Validate.isTrue(0 < parts.length && parts.length < 3,
        "Format of endpoint must be 'port' or 'hostname:port'");

    logger.debug("Parse endpoint {}", endpoint);
    if (parts.length == 1) {
      int port = Integer.parseInt(parts[0]);

      return parseLocalEndPoint(port, localIp);
    } else {
      String hostName = parts[0];
      int port = Integer.parseInt(parts[1]);

      if (hostName.isEmpty()) {
        return parseLocalEndPoint(port, localIp);
      } else {
        validatePort(port);
        return new EndPoint(hostName, port);
      }
    }
  }

  /**
   * Parse instance of {@link EndPoint} from a given string and sub-net.
   *
   * <p>Almost services of py belong to sub-net of control flow. But there is such service
   * 'datanode' who is enable to separate data flow from control flow. That means data flow belongs
   * to different sub-net of control flow. This function parse end-point belongs to sub-net of the
   * given "subnet". Data flow host address could parse by this function.
   */
  public static EndPoint parseInSubnet(String endpoint, String subnet) {
    Validate.notNull(endpoint, "Endpoint to be parsed cannot be null");
    Validate.notEmpty(endpoint, "Endpoint to be parsed cannot be empty");

    String[] parts = endpoint.split(":");
    Validate.isTrue(0 < parts.length && parts.length < 3,
        "Format of endpoint must be 'port' or 'hostname:port'");

    logger.debug("Parse endpoint {}", endpoint);
    if (parts.length == 1) {
      int port = Integer.parseInt(parts[0]);

      return parseInSubnet(port, subnet);
    } else {
      int port = Integer.parseInt(parts[1]);
      validatePort(port);

      try {
        String hostName =
            (parts[0].isEmpty()) ? getLocalHostLanAddress(subnet).getHostAddress() : parts[0];
        return new EndPoint(hostName, port);
      } catch (UnknownHostException e) {
        logger.error("Caught an exception", e);
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Parse instance of {@link EndPoint} from a given port and sub-net.
   *
   * <p>Almost services of py belong to sub-net of control flow. But there is such service
   * 'datanode' who is enable to separate data flow from control flow. That means data flow belongs
   * to different sub-net of control flow. This function parse end-point belongs to sub-net of the
   * given "subnet". Data flow host address could parse by this function.
   */
  public static EndPoint parseInSubnet(int port, String subnet) {
    validatePort(port);

    try {
      logger.debug("Current port: {}, subnet:{}", port, subnet);
      String hostName = getLocalHostLanAddress(subnet).getHostAddress();

      return new EndPoint(hostName, port);
    } catch (UnknownHostException e) {
      logger.error("Caught an exception", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Get network interface info to which an IPV4 address belongs to the given subnet binding.
   *
   * @param subnet subnet
   * @return network interface info to which an IPV4 address belongs to the given subnet binding.
   * @throws UnknownHostException if no network interface being found
   */
  public static NetworkInterface getLocalHostLanInterface(String subnet)
      throws UnknownHostException {
    NetworkInterface netInterface;
    Enumeration<NetworkInterface> interfaceEnumeration;
    // use tree set to sort network interfaces to prevent disagree result among
    // many-times invocation.
    TreeSet<NetworkInterface> ifaces = new TreeSet<>(new IfaceComparator());

    try {
      interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
      while (interfaceEnumeration.hasMoreElements()) {
        InetAddress address;
        Enumeration<InetAddress> addrEnumeration;

        netInterface = interfaceEnumeration.nextElement();
        addrEnumeration = netInterface.getInetAddresses();
        while (addrEnumeration.hasMoreElements()) {
          address = addrEnumeration.nextElement();
          try {
            if (isInSubnet(address.getHostAddress(), subnet)
                && MAIN_IPS.contains(address.getHostAddress())) {
              logger
                  .info("Parsed ip {} binding interface {} in subnet {}", address.getHostAddress(),
                      netInterface.getName(), subnet);
              ifaces.add(netInterface);
            } else {
              logger.info("Address {} is not in subnet {}", address.getHostAddress(), subnet);
            }
          } catch (Exception e) {
            logger.info("Unable to check if address {} is in subnet {}, maybe it is IPV6",
                address.getHostAddress(), subnet);
            continue;
          }
        }
      }

      if (ifaces.size() > 0) {
        logger.info("Select one network iface from parsed ifaces: {}", ifaces.first());
        return ifaces.first();
      }

      logger.error(
          "Unable to find proper IP address after goning through all ifaces, "
              + "try to use java default binding ip address");
      throw new UnknownHostException();
    } catch (Exception e) {
      logger.error("Caught an exception when parse network interface in subnet {}", subnet, e);
      UnknownHostException unknownHostException = new UnknownHostException(
          "Failed to determine LAN interface: " + e);
      unknownHostException.initCause(e);
      throw unknownHostException;
    }
  }

  /**
   * Get one of IPV4 addresses belongs to the given network binding to the given network interface.
   *
   * @param subnet       cidr-notation of network
   * @param networkIface network interface
   * @return one of IPV4 addresses binding to the given network interface.
   * @throws UnknownIpv4HostException if no address found from the given network interface
   */
  public static InetAddress getIpv4Addr(String subnet, NetworkInterface networkIface)
      throws UnknownIpv4HostException {
    InetAddress address;
    Enumeration<InetAddress> addrEnumeration;
    // use tree set to sort network interfaces to prevent disagree result among many-times
    // invocation.
    TreeSet<InetAddress> addresses = new TreeSet<>(new InetAddressComparator());

    networkIface.getSubInterfaces();
    addrEnumeration = networkIface.getInetAddresses();
    while (addrEnumeration.hasMoreElements()) {
      address = addrEnumeration.nextElement();
      try {
        if (isInSubnet(address.getHostAddress(), subnet)
            && MAIN_IPS.contains(address.getHostAddress())) {
          logger.info("address : {}", address.toString());
          addresses.add(address);
        }
      } catch (Exception e) {
        logger.info("Unable to check if address {} is in subnet {}, maybe it is IPV6",
            address.getHostAddress(),
            subnet);
        continue;
      }
    }

    if (addresses.size() > 0) {
      logger.info("Select one address from parsed addresses: {}", addresses.first());
      return addresses.first();
    }

    logger.error(
        "Unable to find proper IP address after goning through all addresses of "
            + "network interface {}",
        networkIface.getName());
    throw new UnknownIpv4HostException();
  }

  /**
   * Get one of IPV6 addresses binding to the given network interface.
   *
   * @param subnet       cidr-notation of ipv6 network
   * @param networkIface network interface
   * @return one of IPV6 addresses binding to the given network interface.
   * @throws UnknownIpv6HostException if no address found from the given network interface
   */
  public static InetAddress getIpv6Addr(String subnet, NetworkInterface networkIface)
      throws UnknownIpv6HostException {
    CidrUtils cidrUtils = null;
    InetAddress address;
    Enumeration<InetAddress> addrEnumeration;
    // use tree set to sort network interfaces to prevent disagree result among
    // many-times invocation.
    TreeSet<InetAddress> addresses = new TreeSet<>(new InetAddressComparator());

    if (subnet != null) {
      try {
        cidrUtils = new CidrUtils(subnet);
      } catch (UnknownHostException e) {
        throw new UnknownIpv6HostException();
      }
    }

    addrEnumeration = networkIface.getInetAddresses();
    while (addrEnumeration.hasMoreElements()) {
      address = addrEnumeration.nextElement();
      if (address instanceof Inet6Address) {
        try {
          if (subnet == null || cidrUtils.isInRange(address.getHostAddress())) {
            logger.warn("Parsed an IPV6 address {} in subnet {}", address.getHostAddress(), subnet);
            addresses.add(address);
          }
        } catch (UnknownHostException e) {
          throw new UnknownIpv6HostException();
        }
      }
    }

    if (addresses.size() > 0) {
      logger.warn("Select one address from parsed addresses: {}", addresses.first());
      return addresses.first();
    }

    logger.error(
        "Unable to find proper IP address after goning through all addresses of "
            + "network interface {}",
        networkIface.getName());
    throw new UnknownIpv6HostException();
  }

  /**
   * Go through all NICs of local host and select one NIC whose binding address belongs to the
   * specified sub-net. If there is no such address, return default jdk supplied address as local
   * host address.
   */
  public static InetAddress getLocalHostLanAddress(String subnet) throws UnknownHostException {
    NetworkInterface networkIface;

    networkIface = getLocalHostLanInterface(subnet);
    return getIpv4Addr(subnet, networkIface);
  }

  /**
   * Check if a IP given in string is in specified subnet in a CIDR-notation string e.g.
   * "192.168.0.1/16".
   */
  public static boolean isInSubnet(String ipInStr, String subnetInStr) throws UnknownHostException {
    ipInStr = ipInStr.trim();

    validateIp(ipInStr);

    int subnet = getSubnet(subnetInStr);
    int maskLen = getMaskLen(subnetInStr);
    int ip = getIp(ipInStr);

    // signed integer left shift 32 bits is negative, but we want zero
    int mask = (maskLen == 0) ? 0 : 0xFFFFFFFF << (32 - maskLen);

    return ((subnet & mask) == (ip & mask));
  }

  /**
   * Get network address from a CIDR-notation string e.g. "192.168.0.1/16".
   */
  public static int getSubnet(String subnetInStr) throws UnknownHostException {
    subnetInStr = subnetInStr.trim();

    String ipInStr = subnetInStr.split("/")[0];

    return getIp(ipInStr);
  }

  /**
   * Get subnet mask len from a CIDR-notation string e.g. "192.168.0.1/16".
   */
  public static int getMaskLen(String subnetInStr) throws UnknownHostException {
    subnetInStr = subnetInStr.trim();

    int maskLen = Integer.valueOf(subnetInStr.split("/")[1]);
    if (maskLen < 0 || maskLen > IPV4_MAX_MASK_LEN) {
      throw new UnknownHostException("Invalid subnet " + subnetInStr);
    }

    return maskLen;
  }

  /**
   * Get IP integer value from its string.
   */
  public static int getIp(String ipInStr) throws UnknownHostException {
    ipInStr = ipInStr.trim();

    validateIp(ipInStr);

    InetAddress inetAddr = InetAddress.getByName(ipInStr);
    ByteBuffer inetAddrBuffer = ByteBuffer.wrap(inetAddr.getAddress());
    inetAddrBuffer.clear();

    return inetAddrBuffer.getInt();
  }

  /**
   * Get IP string from given integer number.
   */
  public static String getIpInStr(int ip) throws UnknownHostException {
    ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
    buf.putInt(ip);
    buf.clear();

    StringBuffer sb = new StringBuffer();
    for (byte ipByte : buf.array()) {
      // transfer negative byte to positive
      sb.append(ipByte & 0x00FF);
      sb.append('.');
    }

    return sb.substring(0, sb.length() - 1);
  }

  private static void validatePort(int port) {
    Validate.isTrue(MIN_PORT <= port && port <= MAX_PORT,
        "Port " + port + " should be between " + MIN_PORT + " and " + MAX_PORT);
  }

  private static void validateIp(String ipInStr) throws UnknownHostException {
    Matcher matcher = addressPattern.matcher(ipInStr);
    if (!matcher.matches()) {
      throw new UnknownHostException("Unknow IP " + ipInStr);
    }
  }

  public static Set<String> secondaryIps() {
    final String cmdIpAll = "ip a";

    SecondaryIpsParser secondaryIpsParser;
    OsCmdOutputLogger errorOutput;

    secondaryIpsParser = new SecondaryIpsParser();

    errorOutput = new OsCmdOutputLogger(cmdIpAll);
    errorOutput.setErrorStream(true);

    try {
      OsCmdExecutor.exec(cmdIpAll, secondaryIpsParser, errorOutput);
    } catch (Exception e) {
      logger.error("Unable to parse secondary IPs", e);
    }

    return secondaryIpsParser.getSecondaryIps();
  }

  public static Set<String> mainIps() {
    final String cmdIfconfig = "ifconfig";
    MainIpsParser mainIpsParser;
    OsCmdOutputLogger errorOutput;

    mainIpsParser = new MainIpsParser();

    errorOutput = new OsCmdOutputLogger(cmdIfconfig);
    errorOutput.setErrorStream(true);

    try {
      OsCmdExecutor.exec(cmdIfconfig, mainIpsParser, errorOutput);
    } catch (Exception e) {
      logger.error("Unable to parse main IPs", e);
    }

    return mainIpsParser.getMainIps();
  }

  public static class UnknownIpv4HostException extends UnknownHostException {
    public UnknownIpv4HostException() {
    }

    public UnknownIpv4HostException(String host) {
      super(host);
    }
  }

  public static class UnknownIpv6HostException extends UnknownHostException {
    public UnknownIpv6HostException() {
    }

    public UnknownIpv6HostException(String host) {
      super(host);
    }
  }

  static class InetAddressComparator implements Comparator<InetAddress> {
    @Override
    public int compare(InetAddress o1, InetAddress o2) {
      return o1.getHostAddress().compareTo(o2.getHostAddress());
    }
  }

  static class IfaceComparator implements Comparator<NetworkInterface> {
    @Override
    public int compare(NetworkInterface o1, NetworkInterface o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  public static class SecondaryIpsParser implements OsCmdStreamConsumer {
    private static final String secondaryIPRegex = "inet[6]*\\s+([^\\s]+)/\\d+.*\\s+secondary\\s+";
    private final Logger logger = LoggerFactory.getLogger(SecondaryIpsParser.class);
    private Set<String> secondaryIps = new HashSet<>();

    @Override
    public void consume(InputStream stream) throws IOException {
      String line;
      BufferedReader br;
      Pattern secondaryIpPat = Pattern.compile(secondaryIPRegex);

      br = new BufferedReader(new InputStreamReader(stream));
      while ((line = br.readLine()) != null) {
        Matcher matcher = secondaryIpPat.matcher(line);

        if (matcher.find()) {
          secondaryIps.add(matcher.group(1));
        }
      }
    }

    public Set<String> getSecondaryIps() {
      for (String ip : secondaryIps) {
        logger.warn("secondary ips : {}", ip);
      }
      return secondaryIps;
    }

    public void setSecondaryIps(Set<String> secondaryIps) {
      this.secondaryIps = secondaryIps;
    }
  }

  public static class MainIpsParser implements OsCmdStreamConsumer {
    private static final String nicRegex = "^([^\\s^:]+):?\\s+.*";
    private static final String mainIPRegex = "inet6?\\s+(addr:\\s*)?([^\\s]+)\\s+.*";
    private final Logger logger = LoggerFactory.getLogger(MainIpsParser.class);
    private Set<String> mainIps = new HashSet<>();

    @Override
    public void consume(InputStream stream) throws IOException {
      String line;
      String nicName = null;
      BufferedReader br;
      Pattern mainIpPat = Pattern.compile(mainIPRegex);
      Pattern nicPat = Pattern.compile(nicRegex);

      br = new BufferedReader(new InputStreamReader(stream));
      while ((line = br.readLine()) != null) {
        Matcher matcher;

        if (nicName == null) { // parse nic name
          matcher = nicPat.matcher(line);
        } else {
          matcher = mainIpPat.matcher(line);
        }

        if (nicName == null && matcher.find()) {
          nicName = matcher.group(1);
          nicName = (nicName.contains(":")) ? null : nicName;
        }

        if (nicName != null && matcher.find()) {
          mainIps.add(matcher.group(matcher.groupCount()));
          nicName = null;
        }
      }
    }

    public Set<String> getMainIps() {
      for (String ip : mainIps) {
        logger.warn("main ips : {}", ip);
      }
      return mainIps;
    }

    public void setMainIps(Set<String> mainIps) {
      this.mainIps = mainIps;
    }
  }
}
