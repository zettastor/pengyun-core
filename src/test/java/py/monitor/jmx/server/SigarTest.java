/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
