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

package py.monitor.jmx.server;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.test.TestBase;

public class SigarTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(SigarTest.class);

  private void printCpuPerc(CpuPerc cpu) {
    logger.debug("\n--------------------------------------------------");
    logger.debug("User:\t {}", CpuPerc.format(cpu.getUser()));
    logger.debug("Sys:\t\t {}", CpuPerc.format(cpu.getSys()));
    logger.debug("Wait:\t {}", CpuPerc.format(cpu.getWait()));
    logger.debug("Nice:\t {}", CpuPerc.format(cpu.getNice()));
    logger.debug("Idle:\t {}", cpu.getIdle());
    logger.debug("Idle:\t {}", CpuPerc.format(cpu.getIdle()));
    logger.debug("Total:\t {}", CpuPerc.format(cpu.getCombined()));
  }

  private void printEthernet(NetInterfaceConfig ethernet) {
    logger.debug("\n\n------------------------------  {}  ----------------------------------",
        ethernet.getName());
    logger.debug("IP address:\t\t\t {}", ethernet.getAddress());
    logger.debug("Gateway broadcast address:\t {}", ethernet.getBroadcast());
    logger.debug("MAC address:\t\t\t {}", ethernet.getHwaddr());
    logger.debug("Net mask:\t\t\t {}", ethernet.getNetmask());
    logger.debug("Description:\t\t\t {}", ethernet.getDescription());
    logger.debug("Network card type:\t\t {}", ethernet.getType());
    logger.debug("\n-------------------------------------------------------------------------\n");
  }

  private void printNet(String name, NetInterfaceStat net) {
    logger
        .debug("\n\n------------------------------  {}  ----------------------------------", name);
    logger.debug("Received package number:\t\t\t {}", net.getRxPackets());
    logger.debug("Sended package number:\t\t\t {}", net.getTxPackets());
    logger.debug("Received data size:\t\t\t\t {}(Byte)", net.getRxBytes());
    logger.debug("Sended data size:\t\t\t\t {}(Byte)", net.getTxBytes());
    logger.debug("Received error package number:\t\t {}", net.getRxErrors());
    logger.debug("Sended error package number:\t\t\t {}", net.getTxErrors());
    logger.debug("Package number been dropped when receive:\t {}", net.getRxDropped());
    logger.debug("Package number been dropped when send:\t {}", net.getTxDropped());
    logger.debug("\n-------------------------------------------------------------------------\n");
  }

}
