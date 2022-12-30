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

package py.zookeeper;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.app.context.AppContext;
import py.instance.InstanceStatus;

public class ZkElectionLeader implements ZkListener {
  private static final Logger logger = LoggerFactory.getLogger(ZkElectionLeader.class);
  private final ZkClientFactory zkClientFactory;
  private final AppContext context;
  private String balletBox;
  private String ticket;
  private byte[] data;
  private String ticketPrefix = "ticket_";
  private ZkClient zkClient;
  private String watchPath;

  public ZkElectionLeader(ZkClientFactory zkClientFactory, String balletBox, AppContext context) {
    this.zkClientFactory = zkClientFactory;
    this.balletBox = balletBox;
    if (balletBox.endsWith("/")) {
      this.balletBox = balletBox.substring(0, balletBox.length() - 1);
    }
    this.context = context;
  }

  public AppContext getAppContext() {
    return context;
  }

  public void startElection() throws ZkException {
    synchronized (this) {
      zkClient = zkClientFactory.generate(this);
    }
  }

  private void init() throws ZkException {
    synchronized (this) {
      zkClient.createPath(balletBox);
      if (ticket == null) {
        ticket = zkClient.createFile(balletBox + "/" + ticketPrefix, data, true);
        logger.warn("balletBox: {}, ticket: {}", balletBox, ticket);
      }

      if (election(ticket)) {
        context.setStatus(InstanceStatus.HEALTHY);
      } else {
        context.setStatus(InstanceStatus.SUSPEND);
      }
    }
  }

  private boolean election(String currentTicket) {
    List<String> children;
    try {
      children = zkClient.getFiles(balletBox);
    } catch (ZkException e) {
      logger.error("can not get ticket from box:{}, current ticket", balletBox, currentTicket);
      return false;
    }

    logger.info("++++++ children: {}, currentTicket: {}", children, currentTicket);
    Collections.sort(children);
    int index = children.indexOf(currentTicket.substring(currentTicket.lastIndexOf('/') + 1));
    if (index == 0) {
      logger
          .warn("now i am leader: {}, ticket: {}, children: {}", zkClientFactory.getServerAddress(),
              currentTicket, children);
      return true;
    } else {
      boolean success = false;
      while (index > 0) {
        try {
          final String watchNode = children.get(--index);
          String watchPath = balletBox + "/" + watchNode;
          zkClient.monitor(watchPath);
          this.watchPath = watchPath;
          success = true;
          logger.warn("now i am fellower, so monitor: {}, my: {}, children: {}", watchPath,
              currentTicket, children);
          break;
        } catch (Exception e) {
          logger.warn("watch node failure, current tick: {}, watch path: {}, all path: {}",
              currentTicket,
              watchPath, children, e);
        }
      }

      if (!success && index == 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void pathDeleted(String path) {
    if (!watchPath.equals(path)) {
      throw new IllegalArgumentException("watchpath: " + watchPath + ", path: " + path);
    }

    logger.info("monitor node: {} is deleted, so became OK", path);
    if (election(ticket)) {
      context.setStatus(InstanceStatus.HEALTHY);
    } else {
      context.setStatus(InstanceStatus.SUSPEND);
    }
  }

  public void close() {
    if (zkClient != null) {
      zkClient.close();
    }
  }

  @Override
  public void expired() {
    try {
      logger.warn("client:{}, ticket:{}, expired", zkClientFactory.getServerAddress(), ticket);

      ticket = null;
      startElection();
    } catch (Exception e) {
      logger.error("caught an exception", e);
    }
  }

  @Override
  public void disconnected() {
    logger.warn("client:{}, disconnected", zkClientFactory.getServerAddress());
    context.setStatus(InstanceStatus.SUSPEND);
  }

  @Override
  public void connected(long sessionId) {
    logger.warn("client: {}, connection successfully, sessionId: {}",
        zkClientFactory.getServerAddress(),
        Long.toHexString(sessionId));
    try {
      init();
    } catch (Exception e) {
      logger.error("caught an exception", e);
    }
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }
}
