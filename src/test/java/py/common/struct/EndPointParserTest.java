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

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPointParser.MainIpsParser;
import py.common.struct.EndPointParser.SecondaryIpsParser;
import py.test.TestBase;

public class EndPointParserTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(EndPointParserTest.class);

  public void init() throws Exception {
    super.init();
  }

  @Test
  public void getMaskLen() throws Exception {
    String[] subnets = {"10.0.1.1/8", "10.0.1.1/16", "10.0.1.1/24", "10.0.1.1/32"};
    int[] masks = {8, 16, 24, 32};

    for (int i = 0; i < subnets.length; i++) {
      String subnet = subnets[i];
      int mask = EndPointParser.getMaskLen(subnet);

      Assert.assertEquals(masks[i], mask);
    }
  }

  @Test
  public void isInSubnet() throws Exception {
    String[] subnets = {"10.0.1.1/8", "192.168.1.1/16", "0.0.0.0/0"};
    String[][] ipsInSubnet = {{"10.0.1.1", "10.0.1.16", "10.0.1.254", "10.255.255.254", "10.0.0.1"},
        {"192.168.1.1", "192.168.26.1", "192.168.255.254"}, {"10.1.1.1"}};
    String[][] ipsNotInSubnet = {{"11.0.0.0", "192.168.26.1"}, {"192.167.1.1", "10.0.1.16"}, {}};

    for (int i = 0; i < subnets.length; i++) {
      String subnet = subnets[i];

      String[] ipsInSubnetI = ipsInSubnet[i];
      for (String ipInSubnet : ipsInSubnetI) {
        Assert.assertTrue(EndPointParser.isInSubnet(ipInSubnet, subnet));
      }

      String[] ipsNotInSubnetI = ipsNotInSubnet[i];
      for (String ipNotInSubnet : ipsNotInSubnetI) {
        logger.debug("Ip {} not in subnet {}", ipNotInSubnet, subnet);
        Assert.assertFalse(EndPointParser.isInSubnet(ipNotInSubnet, subnet));
      }
    }
  }

  @Test
  public void isInSubnetGoThroughAllIp() throws Exception {
    String subnet = "10.0.1.1/8";

    int low = EndPointParser.getIp("10.0.0.0");
    int high = EndPointParser.getIp("10.255.255.255");
    int amount = 0x00FFFFFF + 1;
    Assert.assertEquals(amount, (high - low + 1));

    for (int ip = low; ip <= high; ip++) {
      String ipInstr = ((ip & 0xFF000000) >> 24) + "." + ((ip & 0x00FF0000) >> 16) + "."
          + ((ip & 0x0000FF00) >> 8) + "." + (ip & 0x000000FF);
      Assert.assertTrue(EndPointParser.isInSubnet(ipInstr, subnet));
    }
  }

  @Test
  public void getLocalHostLanAddressForSame() throws Exception {
    String subnet = "0.0.0.0/1";

    InetAddress localAddr = EndPointParser.getLocalHostLanAddress(subnet);
    for (int i = 0; i < 100; i++) {
      Assert.assertEquals(localAddr.getHostAddress(), EndPointParser.getLocalHostLanAddress(subnet)
          .getHostAddress());
    }
    logger.debug("local address: {}", localAddr.getHostAddress());
  }

  @Test
  public void getIpInStr() throws Exception {
    String baseIpInStr = "10.0.1.1";
    int baseIp = EndPointParser.getIp(baseIpInStr);

    Assert.assertEquals("10.0.1.2", EndPointParser.getIpInStr(baseIp + 1));
    Assert.assertEquals("10.0.1.254", EndPointParser.getIpInStr(baseIp + 253));
    Assert.assertEquals("10.0.1.255", EndPointParser.getIpInStr(baseIp + 254));
    Assert.assertEquals("10.0.2.1", EndPointParser.getIpInStr(baseIp + 256));

    baseIpInStr = "0.0.0.1";
    baseIp = EndPointParser.getIp(baseIpInStr);

    Assert.assertEquals("0.0.0.2", EndPointParser.getIpInStr(baseIp + 1));
    Assert.assertEquals("0.0.0.254", EndPointParser.getIpInStr(baseIp + 253));
    Assert.assertEquals("0.0.0.255", EndPointParser.getIpInStr(baseIp + 254));
    Assert.assertEquals("0.0.1.1", EndPointParser.getIpInStr(baseIp + 256));
  }

  @Test
  public void testSecondaryIpsParser() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append(
        "1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1\n"
            + "    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00\n"
            + "    inet 127.0.0.1/8 scope host lo\n"
            + "       valid_lft forever preferred_lft forever\n" + "    inet6 ::1/128 scope host \n"
            + "       valid_lft forever preferred_lft forever\n"
            + "2: enp0s31f6: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP "
            + "group default qlen 1000\n"
            + "    link/ether 1c:1b:0d:8a:d6:9a brd ff:ff:ff:ff:ff:ff\n"
            + "    inet 10.0.1.16/23 brd 10.0.1.255 scope global enp0s31f6\n"
            + "       valid_lft forever preferred_lft forever\n"
            + "    inet6 fe80::ed17:1be1:21c3:824b/64 scope link \n"
            + "       valid_lft forever preferred_lft forever\n");
    sb.append(
        "3: eth1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000\n"
            + "    link/ether 08:00:27:d3:c9:30 brd ff:ff:ff:ff:ff:ff\n"
            + "    inet 10.0.10.13/24 brd 10.0.10.255 scope global eth1\n"
            + "       valid_lft forever preferred_lft forever\n"
            + "    inet 10.0.10.101/24 scope global secondary eth1\n"
            + "       valid_lft forever preferred_lft forever\n"
            + "    inet 10.0.10.102/24 scope global secondary eth1\n"
            + "       valid_lft forever preferred_lft forever\n"
            + "    inet6 fe80::a00:27ff:fed3:c930/64 scope link\n"
            + "       valid_lft forever preferred_lft forever\n");

    ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
    SecondaryIpsParser parser = new SecondaryIpsParser();
    parser.consume(bais);

    Assert.assertEquals(2, parser.getSecondaryIps().size());
    Assert.assertTrue(parser.getSecondaryIps().contains("10.0.10.101"));
    Assert.assertTrue(parser.getSecondaryIps().contains("10.0.10.102"));
  }

  @Test
  public void testMainIpsParser() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500\n"
        + "        inet 10.0.0.223  netmask 255.0.0.0  broadcast 10.255.255.255\n"
        + "        inet6 fe80::5054:ff:fead:36cb  prefixlen 64  scopeid 0x20<link>\n"
        + "        ether 52:54:00:ad:36:cb  txqueuelen 1000  (Ethernet)\n"
        + "        RX packets 3694736  bytes 451287383 (430.3 MiB)\n"
        + "        RX errors 0  dropped 6042  overruns 0  frame 0\n"
        + "        TX packets 3700897  bytes 449046566 (428.2 MiB)\n"
        + "        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0\n" + "\n"
        + "eth0:15: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500\n"
        + "        inet 10.0.0.227  netmask 255.0.0.0  broadcast 10.255.255.255\n"
        + "        ether 52:54:00:ad:36:cb  txqueuelen 1000  (Ethernet)\n" + "\n"
        + "eth0:49: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500\n"
        + "        inet 10.0.0.228  netmask 255.0.0.0  broadcast 10.255.255.255\n"
        + "        ether 52:54:00:ad:36:cb  txqueuelen 1000  (Ethernet)\n" + "\n"
        + "lo: flags=73<UP,LOOPBACK,RUNNING>  mtu 65536\n"
        + "        inet 127.0.0.1  netmask 255.0.0.0\n"
        + "        inet6 ::1  prefixlen 128  scopeid 0x10<host>\n"
        + "        loop  txqueuelen 0  (Local Loopback)\n"
        + "        RX packets 3145926  bytes 323914506 (308.9 MiB)\n"
        + "        RX errors 0  dropped 0  overruns 0  frame 0\n"
        + "        TX packets 3145926  bytes 323914506 (308.9 MiB)\n"
        + "        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0");

    ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
    MainIpsParser parser = new MainIpsParser();
    parser.consume(bais);

    Assert.assertTrue(parser.getMainIps().contains("10.0.0.223"));
    Assert.assertFalse(parser.getMainIps().contains("10.0.0.227"));
    Assert.assertFalse(parser.getMainIps().contains("10.0.0.228"));
    Assert.assertFalse(parser.getMainIps().contains("10.0.0.229"));
  }

  @Test
  public void testMainIpsParser_Ubuntu16_04() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("br0       Link encap:Ethernet  HWaddr e0:d5:5e:55:80:36  \n"
        + "          inet addr:10.0.1.79  Bcast:10.0.1.255  Mask:255.255.254.0\n"
        + "          inet6 addr: fe80::5e9a:db0e:1489:b42a/64 Scope:Link\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:3478123 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:1575746 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:504028126 (504.0 MB)  TX bytes:2459369898 (2.4 GB)\n"
        + "\n"
        + "br0:0     Link encap:Ethernet  HWaddr e0:d5:5e:55:80:36  \n"
        + "          inet addr:192.168.1.100  Bcast:192.168.1.255  Mask:255.255.255.0\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "\n"
        + "enp0s31f6:1 Link encap:Ethernet  HWaddr e0:d5:5e:55:80:36  \n"
        + "          inet addr:10.0.1.78  Bcast:10.0.1.255  Mask:255.255.254.0\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:4012998 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:1707216 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:655766416 (655.7 MB)  TX bytes:491595181 (491.5 MB)\n"
        + "          Interrupt:16 Memory:f7000000-f7020000 \n"
        + "\n"
        + "lo        Link encap:Local Loopback  \n"
        + "          inet addr:127.0.0.1  Mask:255.0.0.0\n"
        + "          inet6 addr: ::1/128 Scope:Host\n"
        + "          UP LOOPBACK RUNNING  MTU:65536  Metric:1\n"
        + "          RX packets:2406294 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:2406294 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1 \n"
        + "          RX bytes:910728637 (910.7 MB)  TX bytes:910728637 (910.7 MB)\n"
        + "\n"
        + "virbr0    Link encap:Ethernet  HWaddr 00:00:00:00:00:00  \n"
        + "          inet addr:192.168.2.1  Bcast:192.168.2.255  Mask:255.255.255.0\n"
        + "          UP BROADCAST MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:0 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)\n"
        + "vnet0     Link encap:Ethernet  HWaddr fe:54:00:99:37:b4  \n"
        + "          inet6 addr: fe80::fc54:ff:fe99:37b4/64 Scope:Link\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:15500978 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:38492783 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:2136081034 (2.1 GB)  TX bytes:5308629920 (5.3 GB)\n"
        + "\n"
        + "vnet1     Link encap:Ethernet  HWaddr fe:54:00:e5:d8:1d  \n"
        + "          inet6 addr: fe80::fc54:ff:fee5:d81d/64 Scope:Link\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:10 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:2396085 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:708 (708.0 B)  TX bytes:154085602 (154.0 MB)\n"
        + "\n"
        + "vnet2     Link encap:Ethernet  HWaddr fe:54:00:d6:b6:87  \n"
        + "          inet6 addr: fe80::fc54:ff:fed6:b687/64 Scope:Link\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:29571038 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:25684831 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:3558877307 (3.5 GB)  TX bytes:3341750018 (3.3 GB)\n"
        + "\n"
        + "vnet3     Link encap:Ethernet  HWaddr fe:54:00:d4:9f:a7  \n"
        + "          inet6 addr: fe80::fc54:ff:fed4:9fa7/64 Scope:Link\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:29294276 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:25701449 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:3537024502 (3.5 GB)  TX bytes:3343180279 (3.3 GB)\n"
        + "vnet4     Link encap:Ethernet  HWaddr fe:54:00:5d:ac:d7  \n"
        + "          inet6 addr: fe80::fc54:ff:fe5d:acd7/64 Scope:Link\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:29209196 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:25491997 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:3530821580 (3.5 GB)  TX bytes:3323520670 (3.3 GB)\n"
        + "\n"
        + "vnet5     Link encap:Ethernet  HWaddr fe:54:00:fc:30:1a  \n"
        + "          inet6 addr: fe80::fc54:ff:fefc:301a/64 Scope:Link\n"
        + "          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n"
        + "          RX packets:31487222 errors:0 dropped:0 overruns:0 frame:0\n"
        + "          TX packets:25580093 errors:0 dropped:0 overruns:0 carrier:0\n"
        + "          collisions:0 txqueuelen:1000 \n"
        + "          RX bytes:3789745769 (3.7 GB)  TX bytes:3601914249 (3.6 GB)\n");

    ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
    MainIpsParser parser = new MainIpsParser();
    parser.consume(bais);
    Set<String> parserIpSet = parser.getMainIps();

    Assert.assertTrue(parserIpSet.contains("10.0.1.79"));
    Assert.assertTrue(parserIpSet.contains("127.0.0.1"));
    Assert.assertTrue(parserIpSet.contains("192.168.2.1"));
    Assert.assertTrue(parserIpSet.contains("fe80::fc54:ff:fed6:b687/64"));
    Assert.assertTrue(parserIpSet.contains("fe80::fc54:ff:fe99:37b4/64"));
    Assert.assertTrue(parserIpSet.contains("fe80::fc54:ff:fed4:9fa7/64"));
    Assert.assertTrue(parserIpSet.contains("fe80::fc54:ff:fe5d:acd7/64"));
    Assert.assertTrue(parserIpSet.contains("fe80::fc54:ff:fefc:301a/64"));
    Assert.assertTrue(parserIpSet.contains("fe80::fc54:ff:fefc:301a/64"));
    Assert.assertTrue(parserIpSet.contains("fe80::fc54:ff:fee5:d81d/64"));

    Assert.assertFalse(parserIpSet.contains("10.0.1.78"));
    Assert.assertFalse(parserIpSet.contains("192.168.1.100"));
    Assert.assertFalse(parserIpSet.contains("192.168.2.2"));
    Assert.assertFalse(parserIpSet.contains("fe81::fc54:ff:fee5:d81d/64"));
  }

}
