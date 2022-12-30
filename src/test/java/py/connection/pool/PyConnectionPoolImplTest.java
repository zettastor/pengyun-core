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

package py.connection.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.common.struct.Pair;
import py.connection.pool.udp.detection.NetworkIoHealthChecker;
import py.engine.DelayedTask;
import py.engine.DelayedTaskEngine;
import py.engine.Result;
import py.engine.ResultImpl;
import py.test.TestBase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PyConnectionPoolImplTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(PyConnectionPoolImplTest.class);

  private final int maxDelayMs = 5;

  @Mock
  BootstrapBuilder bootstrapBuilder;

  @Mock
  Bootstrap bootstrap;

  PyChannel pyChannel;

  @Mock
  Channel channel;

  @Mock
  ChannelFuture channelFuture;

  DelayedTaskEngine delayedTaskEngine;
  EndPoint endPoint = new EndPoint("127.0.0.1", 6666);
  private SimpleChannelFuture simpleChannelFuture;
  private PyConnectionPoolImpl pyConnectionPool = new PyConnectionPoolImpl(1, 30000,
      bootstrapBuilder);

  @Before
  public void before() {
    pyChannel = new PyChannel(endPoint);
    this.delayedTaskEngine = new DelayedTaskEngine();
    delayedTaskEngine.start();

    this.simpleChannelFuture = new SimpleChannelFuture();
    when(channel.close()).thenReturn(channelFuture);
    when(bootstrapBuilder.build()).thenReturn(bootstrap);
    when(bootstrap.connect(any(String.class), any(Integer.class))).thenReturn(channelFuture);
  }

  @Test
  public void test1RequestComingWhenChannelReconnect() throws Exception {
    pyConnectionPool.close();
    ConnectionRequest request = mock(PyConnectionPoolImpl.InnerChannelRequest.class);

    when(request.getPyChannel()).thenReturn(pyChannel);
    when(channel.isActive()).thenReturn(false);
    NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> true);

    pyConnectionPool.processReconnect(request);

    assertEquals(1, pyConnectionPool.getMapChannelToListener().size());
    ConnectionListenerManager manager = pyConnectionPool.getMapChannelToListener().get(pyChannel);
    assertEquals(0, manager.getRequests().size());
    assertEquals(true, manager.isConnecting());

    ConnectionRequest innerRequest = mock(PyConnectionPoolImpl.InnerChannelRequest.class);
    when(innerRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(innerRequest);
    assertEquals(0, manager.getRequests().size());
    assertEquals(true, manager.isConnecting());

    ConnectionRequest msgRequest = mock(ConnectionRequest.class);
    when(msgRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(msgRequest);
    assertEquals(1, manager.getRequests().size());
    assertEquals(true, manager.isConnecting());

    manager.operationComplete(channelFuture);
    pyChannel.set(channel);

    ConnectionRequest request1 = mock(PyConnectionPoolImpl.ConnectionRequestResult.class);
    when(request1.getPyChannel()).thenReturn(pyChannel);

    pyConnectionPool.processReconnect(request1);
    assertEquals(false, manager.isConnecting());
    assertEquals(0, pyConnectionPool.getMapChannelToListener().size());

    verify(msgRequest, times(1)).complete(channel);

    when(channel.isActive()).thenReturn(true);

    ConnectionRequest msgRequest1 = mock(ConnectionRequest.class);
    when(msgRequest1.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(msgRequest1);
    assertEquals(0, pyConnectionPool.getMapChannelToListener().size());
    verify(msgRequest1, times(1)).complete(channel);

    when(channel.isActive()).thenReturn(false);
    NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> false);

    ConnectionRequest msgRequest2 = mock(ConnectionRequest.class);
    when(msgRequest2.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(msgRequest2);
    assertEquals(0, pyConnectionPool.getMapChannelToListener().size());
    verify(msgRequest2, times(1)).complete(null);
  }

  @Test
  public void test2MsgRequestComingWhenChannelClosing() throws Exception {
    pyConnectionPool.close();
    ConnectionRequest request = mock(PyConnectionPoolImpl.InnerChannelRequest.class);

    when(request.getPyChannel()).thenReturn(pyChannel);
    when(channel.isActive()).thenReturn(false);
    NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> true);
    pyChannel.set(channel);

    pyConnectionPool.processReconnect(request);
    pyChannel.set(channel);
    verify(channel, times(1)).close();
    assertTrue(pyChannel.isClosing());

    assertEquals(1, pyConnectionPool.getMapChannelToListener().size());
    ConnectionListenerManager manager = pyConnectionPool.getMapChannelToListener().get(pyChannel);
    assertEquals(0, manager.getRequests().size());
    assertEquals(false, manager.isConnecting());

    ConnectionRequest msgRequest = mock(ConnectionRequest.class);
    when(msgRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(msgRequest);
    assertEquals(1, manager.getRequests().size());
    assertTrue(pyChannel.isClosing());
    assertEquals(false, manager.isConnecting());

    verify(msgRequest, times(0)).complete(channel);

    ConnectionRequest reconnectAfterCloseDoneRequest = mock(
        PyConnectionPoolImpl.ReconnectAfterCloseDoneRequestResult.class);
    when(reconnectAfterCloseDoneRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(reconnectAfterCloseDoneRequest);

    assertTrue(!pyChannel.isClosing());
    assertEquals(true, manager.isConnecting());
    verify(msgRequest, times(0)).complete(channel);

    manager.operationComplete(channelFuture);
    pyChannel.set(channel);

    ConnectionRequest request1 = mock(PyConnectionPoolImpl.ConnectionRequestResult.class);
    when(request1.getPyChannel()).thenReturn(pyChannel);

    pyConnectionPool.processReconnect(request1);
    assertEquals(false, manager.isConnecting());
    assertEquals(0, pyConnectionPool.getMapChannelToListener().size());

    verify(msgRequest, times(1)).complete(any());

  }

  @Test
  public void test3InnerRequestComingWhenChannelClosing() throws Exception {
    ConnectionRequest request = mock(PyConnectionPoolImpl.InnerChannelRequest.class);

    when(request.getPyChannel()).thenReturn(pyChannel);
    when(channel.isActive()).thenReturn(false);
    NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> true);
    pyChannel.set(channel);

    pyConnectionPool.processReconnect(request);
    verify(channel, times(1)).close();
    assertTrue(pyChannel.isClosing());

    assertEquals(1, pyConnectionPool.getMapChannelToListener().size());
    ConnectionListenerManager manager = pyConnectionPool.getMapChannelToListener().get(pyChannel);
    assertEquals(0, manager.getRequests().size());
    assertEquals(false, manager.isConnecting());

    pyChannel.set(channel);
    ConnectionRequest innerChannelRequest = mock(PyConnectionPoolImpl.InnerChannelRequest.class);
    when(innerChannelRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(innerChannelRequest);
    assertEquals(0, manager.getRequests().size());
    assertTrue(pyChannel.isClosing());
    assertEquals(false, manager.isConnecting());

    verify(innerChannelRequest, times(0)).complete(channel);

    ConnectionRequest reconnectAfterCloseDoneRequest = mock(
        PyConnectionPoolImpl.ReconnectAfterCloseDoneRequestResult.class);
    when(reconnectAfterCloseDoneRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(reconnectAfterCloseDoneRequest);

    assertTrue(!pyChannel.isClosing());
    assertEquals(true, manager.isConnecting());

    manager.operationComplete(channelFuture);
    pyChannel.set(channel);

    ConnectionRequest request1 = mock(PyConnectionPoolImpl.ConnectionRequestResult.class);
    when(request1.getPyChannel()).thenReturn(pyChannel);

    pyConnectionPool.processReconnect(request1);
    assertEquals(false, manager.isConnecting());
    assertEquals(0, pyConnectionPool.getMapChannelToListener().size());

    verify(innerChannelRequest, times(0)).complete(channel);

  }

  @Test
  public void test4ExitChannelRequestComingWhenChannelClosing() throws Exception {
    ConnectionRequest request = mock(PyConnectionPoolImpl.InnerChannelRequest.class);

    when(request.getPyChannel()).thenReturn(pyChannel);
    when(channel.isActive()).thenReturn(false);
    NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> true);
    pyChannel.set(channel);

    pyConnectionPool.processReconnect(request);
    verify(channel, times(1)).close();
    assertTrue(pyChannel.isClosing());

    assertEquals(1, pyConnectionPool.getMapChannelToListener().size());
    ConnectionListenerManager manager = pyConnectionPool.getMapChannelToListener().get(pyChannel);
    assertEquals(0, manager.getRequests().size());
    assertEquals(false, manager.isConnecting());

    ConnectionRequest exitChannelRequest = mock(PyConnectionPoolImpl.ExitChannelRequest.class);
    when(exitChannelRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(exitChannelRequest);
    verify(channel, times(1)).close();
    assertEquals(0, manager.getRequests().size());
    assertTrue(pyChannel.isClosing());
    assertEquals(false, manager.isConnecting());

    ConnectionRequest reconnectAfterCloseDoneRequest = mock(
        PyConnectionPoolImpl.ReconnectAfterCloseDoneRequestResult.class);
    when(reconnectAfterCloseDoneRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(reconnectAfterCloseDoneRequest);

    assertTrue(!pyChannel.isClosing());
    assertEquals(false, manager.isConnecting());
    assertEquals(0, pyConnectionPool.getMapChannelToListener().size());

  }

  @Test
  public void test5CloseChannelRequestComingWhenChannelClosing() throws Exception {
    ConnectionRequest request = mock(PyConnectionPoolImpl.InnerChannelRequest.class);

    when(request.getPyChannel()).thenReturn(pyChannel);
    when(channel.isActive()).thenReturn(false);
    NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> true);
    pyChannel.set(channel);

    pyConnectionPool.processReconnect(request);
    verify(channel, times(1)).close();
    assertTrue(pyChannel.isClosing());

    assertEquals(1, pyConnectionPool.getMapChannelToListener().size());
    ConnectionListenerManager manager = pyConnectionPool.getMapChannelToListener().get(pyChannel);
    assertEquals(0, manager.getRequests().size());
    assertEquals(false, manager.isConnecting());

    ConnectionRequest closeChannelRequest = mock(PyConnectionPoolImpl.CloseChannelRequest.class);
    pyChannel.setLastIoTime(0);
    when(closeChannelRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(closeChannelRequest);

    assertNull(pyChannel.get());
    verify(channel, times(1)).close();
    assertEquals(0, manager.getRequests().size());
    assertTrue(pyChannel.isClosing());
    assertEquals(false, manager.isConnecting());

    ConnectionRequest reconnectAfterCloseDoneRequest = mock(
        PyConnectionPoolImpl.ReconnectAfterCloseDoneRequestResult.class);
    when(reconnectAfterCloseDoneRequest.getPyChannel()).thenReturn(pyChannel);
    pyConnectionPool.processReconnect(reconnectAfterCloseDoneRequest);

    assertTrue(!pyChannel.isClosing());
    assertEquals(true, manager.isConnecting());

    manager.operationComplete(channelFuture);
    pyChannel.set(channel);

    ConnectionRequest request1 = mock(PyConnectionPoolImpl.ConnectionRequestResult.class);
    when(request1.getPyChannel()).thenReturn(pyChannel);

    pyConnectionPool.processReconnect(request1);
    assertTrue(!pyChannel.isClosing());
    assertEquals(false, manager.isConnecting());
    assertEquals(0, pyConnectionPool.getMapChannelToListener().size());
  }

  @Test
  public void test6MsgRequestComeWithAutoConnect() throws Exception {
    pyConnectionPool.retrieveForUnitTest();
    when(channel.close()).thenReturn(simpleChannelFuture);
    when(bootstrap.connect(any(String.class), any(Integer.class))).thenReturn(simpleChannelFuture);

    ConnectionRequest request = mock(ConnectionRequest.class);

    when(request.getPyChannel()).thenReturn(pyChannel);
    when(channel.isActive()).thenReturn(false);
    NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> true);

    pyConnectionPool.processReconnect(request);
    verify(bootstrap, times(1)).connect(endPoint.getHostName(), endPoint.getPort());

    Thread.sleep(maxDelayMs * 20);

    assertEquals(0, pyConnectionPool.getMapChannelToListener().size());
    verify(request, times(1)).complete(channel);
  }

  @Test
  public void test0AllKindsOfRequestComeWithAutoConnectAndClose() throws Exception {
    when(channel.close()).thenReturn(simpleChannelFuture);
    when(bootstrap.connect(any(String.class), any(Integer.class))).thenReturn(simpleChannelFuture);
    NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> true);
    pyConnectionPool.retrieveForUnitTest();

    Map<EndPoint, PyChannelPool> mapEndPointToChannelPool = new HashMap<>();
    PyChannelPool pyChannelPool = mock(PyChannelPool.class);
    when(pyChannelPool.getEndpoint()).thenReturn(endPoint);
    when(pyChannelPool.get(endPoint)).thenReturn(pyChannel);
    mapEndPointToChannelPool.put(endPoint, pyChannelPool);

    pyConnectionPool.setMapEndPointToChannelPool(mapEndPointToChannelPool);

    List<ConnectionRequest> msgRequestList = new ArrayList<>();
    int msgRequestCount = 50;
    for (int i = 0; i < msgRequestCount; i++) {
      ConnectionRequest msgRequest = mock(ConnectionRequest.class);
      when(msgRequest.getPyChannel()).thenReturn(pyChannel);
      msgRequestList.add(msgRequest);
    }

    int closeRequestCount = 10;
    List<ConnectionRequest> closeRequestList = new ArrayList<>();
    for (int j = 0; j < closeRequestCount; j++) {
      ConnectionRequest closeRequest = mock(PyConnectionPoolImpl.CloseChannelRequest.class);
      when(closeRequest.getPyChannel()).thenReturn(pyChannel);
      closeRequestList.add(closeRequest);
    }

    int exitRequestCount = 10;
    List<ConnectionRequest> exitRequestList = new ArrayList<>();
    for (int j = 0; j < exitRequestCount; j++) {
      ConnectionRequest exitRequest = mock(PyConnectionPoolImpl.ExitChannelRequest.class);
      when(exitRequest.getPyChannel()).thenReturn(pyChannel);
      exitRequestList.add(exitRequest);
    }

    List<ConnectionRequest> allRequestList = new ArrayList<>();
    allRequestList.addAll(msgRequestList);
    allRequestList.addAll(closeRequestList);
    allRequestList.addAll(exitRequestList);
    Collections.shuffle(allRequestList);

    List<Pair<ConnectionRequest, Boolean>> assertResultList = new ArrayList<>();
    for (ConnectionRequest connectionRequest : allRequestList) {
      boolean detectResult;
      if (RandomUtils.nextBoolean()) {
        detectResult = true;
      } else {
        detectResult = false;
      }
      NetworkIoHealthChecker.INSTANCE.setResultForTestsOnly(whatever -> detectResult);
      Thread.sleep(PyConnectionPoolImpl.DEFAULT_CHECK_CONNECTION_INTERVAL_MS * 5);
      Pair<ConnectionRequest, Boolean> result = new Pair<>(connectionRequest, detectResult);
      if (!(connectionRequest instanceof PyConnectionPoolImpl.CloseChannelRequest
          || connectionRequest instanceof PyConnectionPoolImpl.ExitChannelRequest)) {
        assertResultList.add(result);
      }
      pyConnectionPool.reconnect(connectionRequest);
      pyChannel.set(channel);
    }
    Thread.sleep(maxDelayMs * 20);
    int loopCount = 0;
    for (Pair<ConnectionRequest, Boolean> assertResult : assertResultList) {
      logger.warn("loop count:{}, assert result:{}", loopCount++, assertResult.getSecond());
      verify(assertResult.getFirst(), times(1)).complete(any());
    }

  }

  private class ListenerTask extends DelayedTask {
    private ChannelFuture channelFuture;
    private GenericFutureListener connectionListenerManager;

    public ListenerTask(int delayMs, GenericFutureListener connectionListenerManager,
        ChannelFuture channelFuture) {
      super(delayMs);
      this.connectionListenerManager = connectionListenerManager;
      this.channelFuture = channelFuture;
    }

    @Override
    public Result work() {
      try {
        pyChannel.set(channel);
        this.connectionListenerManager.operationComplete(channelFuture);
      } catch (Exception e) {
        logger.error("caught exception", e);
      }
      return new ResultImpl();
    }
  }

  private class SimpleChannelFuture implements ChannelFuture {
    @Override
    public Channel channel() {
      return channel;
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public boolean isCancellable() {
      return false;
    }

    @Override
    public Throwable cause() {
      return null;
    }

    @Override
    public ChannelFuture addListener(
        GenericFutureListener<? extends Future<? super Void>> listener) {
      int delayMs = RandomUtils.nextInt(maxDelayMs);
      DelayedTask task = new ListenerTask(delayMs, listener, this);
      delayedTaskEngine.drive(task);
      return this;
    }

    @Override
    public ChannelFuture addListeners(
        GenericFutureListener<? extends Future<? super Void>>[] listeners) {
      return null;
    }

    @Override
    public ChannelFuture removeListener(
        GenericFutureListener<? extends Future<? super Void>> listener) {
      return null;
    }

    @Override
    public ChannelFuture removeListeners(
        GenericFutureListener<? extends Future<? super Void>>[] listeners) {
      return null;
    }

    @Override
    public ChannelFuture sync() throws InterruptedException {
      return null;
    }

    @Override
    public ChannelFuture syncUninterruptibly() {
      return null;
    }

    @Override
    public ChannelFuture await() throws InterruptedException {
      return null;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
      return false;
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
      return false;
    }

    @Override
    public ChannelFuture awaitUninterruptibly() {
      return null;
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
      return false;
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
      return false;
    }

    @Override
    public Void getNow() {
      return null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
      return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      return null;
    }

    @Override
    public boolean isVoid() {
      return false;
    }
  }

}
