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

package py.message.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.message.IfaceMessageHandler;
import py.message.InterfaceMessage;
import py.message.InterfaceReceiver;
import py.message.exceptions.MessageHandlerMissingException;
import py.message.exceptions.MessageMapMissingException;

public class MessageDispatcher {
  private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

  private static final int DEFAULT_DISPATCHER_SIZE = 3;
  private int engineSize = 0;

  private Map<InterfaceMessage<?>, Set<InterfaceReceiver>> messageToObjectMap = null;
  private Set<InterfaceReceiver> listeners;
  private ExecutorService dispatcherExecutorPool;
  private Thread dispatcherThread;
  private boolean shutdownDispatcher = false;
  private boolean dispatcherStarted = false;

  private MessageDispatcher() {
    messageToObjectMap = new ConcurrentHashMap<InterfaceMessage<?>, Set<InterfaceReceiver>>();
    listeners = new HashSet<InterfaceReceiver>();
  }

  public static MessageDispatcher getInstance() {
    return LazyHolder.singletonInstance;
  }

  protected synchronized void addLisener(InterfaceReceiver receiver) {
    listeners.add(receiver);
  }

  protected synchronized void removeLisener(InterfaceReceiver receiver) {
    listeners.remove(receiver);
  }

  protected void add(InterfaceMessage<?> message, InterfaceReceiver receiver) {
    logger.debug("regist message : {} with {}", message, receiver);
    Set<InterfaceReceiver> receivers = messageToObjectMap.get(message);
    if (receivers != null) {
      receivers.add(receiver);
    } else {
      receivers = new HashSet<InterfaceReceiver>();
      receivers.add(receiver);
      messageToObjectMap.put(message, receivers);
    }
  }

  protected void add(InterfaceMessage<?> message) {
    logger.debug("register message : {} with all receivers", message);
    messageToObjectMap.put(message, listeners);
  }

  public void start() {
    if (dispatcherStarted) {
      logger.debug("Dispatcher has already been started");
      return;
    }
    logger.debug("Going to start message dispatcher");

    int trueEngineSize = engineSize == 0 ? DEFAULT_DISPATCHER_SIZE : this.engineSize;
    dispatcherExecutorPool = Executors.newFixedThreadPool(trueEngineSize);

    logger.debug("Start message dispatcher");
    dispatcherThread = new Thread() {
      public void run() {
        while (!shutdownDispatcher) {
          for (Entry<InterfaceMessage<?>, Set<InterfaceReceiver>> entry
              : messageToObjectMap.entrySet()) {
            logger.debug("dispatcher thread is running, there are {} messages to be done",
                messageToObjectMap.size());
            InterfaceMessage<?> message = entry.getKey();
            Set<InterfaceReceiver> receivers = entry.getValue();

            try {
              logger.debug("Directionally send message {} to {}", message, receivers);
              dispatcherExecutorPool.execute(new HandleMessageTask(message, receivers));
              messageToObjectMap.remove(message);
            } catch (RejectedExecutionException e) {
              logger.warn("Caught an exception", e);
            }
          }

          try {
            sleep(5);
          } catch (InterruptedException e) {
            logger.warn("Caught an exception", e);
          }
        }
      }
    };
    dispatcherThread.start();

    dispatcherStarted = true;
  }

  public void stop() {
    logger.debug("Going to stop message pump");

    shutdownDispatcher = true;
    dispatcherThread.interrupt();

    if (dispatcherExecutorPool != null) {
      dispatcherExecutorPool.shutdown();
    }

    logger.debug("Message pump has been stopped");
  }

  private static class LazyHolder {
    private static final MessageDispatcher singletonInstance = new MessageDispatcher();
  }

  private class HandleMessageTask implements Runnable {
    private InterfaceMessage<?> message;
    private Set<InterfaceReceiver> receivers;

    HandleMessageTask(InterfaceMessage<?> message, Set<InterfaceReceiver> receivers) {
      this.message = message;
      this.receivers = receivers;
    }

    @Override
    public synchronized void run() {
      try {
        for (InterfaceReceiver receiver : receivers) {
          Map<Class<?>, IfaceMessageHandler> messageMap = receiver.getMessageMap();
          boolean messageMatched = false;
          for (Entry<Class<?>, IfaceMessageHandler> entry : messageMap.entrySet()) {
            Class<?> message = entry.getKey();
            IfaceMessageHandler handler = entry.getValue();

            if (handler == null) {
              logger.warn("invalid handler");
              throw new MessageHandlerMissingException();
            } else {
              if (this.message.getClass().getName().equals(message.getName())) {
                logger.debug("Message handler has been found");
                messageMatched = true;
                handler.execute(this.message);

                break;
              } else {
                logger.debug("No message handler has been found for {} on message {}", receivers,
                    message);
              }
            }
          }

        }

        if (dispatcherExecutorPool.isShutdown()) {
          return;
        }
      } catch (MessageMapMissingException e) {
        logger.warn("Caught an exception", e);
      } catch (MessageHandlerMissingException e) {
        logger.warn("Caught an exception", e);
      } catch (Exception e) {
        logger.error("Caught an exception", e);
      }
    }
  }

}
