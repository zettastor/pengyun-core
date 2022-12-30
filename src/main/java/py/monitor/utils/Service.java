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

package py.monitor.utils;

import static py.monitor.utils.Service.State.FAILED;
import static py.monitor.utils.Service.State.NEW;
import static py.monitor.utils.Service.State.PAUSED;
import static py.monitor.utils.Service.State.RUNNING;
import static py.monitor.utils.Service.State.STARTING;
import static py.monitor.utils.Service.State.STOPPED;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Service {
  private static final Logger logger = LoggerFactory.getLogger(Service.class);
  private final AtomicReference<State> state = new AtomicReference<State>(NEW);

  public String getState() {
    return state.toString();
  }

  public final void start() throws Exception {
    if (!isJmxAgentSwitcherOn()) {
      logger.warn("JmxAgentSwitcher is off");
      return;
    }

    if (!(state.compareAndSet(NEW, STARTING) || state.compareAndSet(STOPPED, STARTING))) {
      logger.error("Unable to start service with state {}", state);
      throw new IllegalStateException();
    }

    try {
      logger.debug("Current service status: {}", state);
      onStart();
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      if (!(state.compareAndSet(STARTING, FAILED))) {
        throw new IllegalStateException(
            "Fail to set service status to FAILED when failed to start the service");
      }
      throw e;
    }

    if (!state.compareAndSet(STARTING, RUNNING)) {
      logger
          .error("Service has been started success, but unable to set service state to {}", state);
      throw new IllegalStateException();
    }
  }

  public final void run() throws Exception {
    switch (state.get()) {
      case STARTING:
      case PAUSED:
      case STOPPED:
      case FAILED:
        state.set(RUNNING);
        break;
      default:
        throw new IllegalStateException("Unable to run service with state " + state);
    }
  }

  public final void pause() throws Exception {
    if (!state.compareAndSet(RUNNING, PAUSED)) {
      logger.error("Unable to start service with state {}", state);
      throw new IllegalStateException();
    }
    onPause();
  }

  public final void resume() throws Exception {
    if (!state.compareAndSet(PAUSED, RUNNING)) {
      logger.error("Unable to start service with state {}", state);
      throw new IllegalStateException();
    }
    onResume();
  }

  public final void stop() throws Exception {
    if (!(state.compareAndSet(PAUSED, STOPPED) || state.compareAndSet(RUNNING, STOPPED))) {
      logger.error("Unable to stop service with state {}", state);
      throw new IllegalStateException();
    }
    onStop();
  }

  public final void terminate() throws Exception {
    if (state.get() == State.TERMINATED) {
      logger.error("Unable to start service with state {}", state);
      throw new IllegalStateException();
    }
    state.set(State.TERMINATED);
  }

  protected boolean isJmxAgentSwitcherOn() throws Exception {
    return true;
  }

  protected abstract void onStart() throws Exception;

  protected abstract void onStop() throws Exception;

  protected abstract void onPause() throws Exception;

  protected abstract void onResume() throws Exception;

  public static enum State {
    NEW {
      @Override
      protected Set<State> getAllowed() {
        return EnumSet.of(STARTING, RUNNING, STOPPED, FAILED, TERMINATED);
      }
    },
    STARTING {
      @Override
      protected Set<State> getAllowed() {
        return EnumSet.of(RUNNING, STOPPED, FAILED, TERMINATED);
      }
    },
    RUNNING {
      @Override
      protected Set<State> getAllowed() {
        return EnumSet.of(PAUSED, STOPPED, FAILED, TERMINATED);
      }
    },
    PAUSED {
      @Override
      protected Set<State> getAllowed() {
        return EnumSet.of(RUNNING, STOPPED, FAILED, TERMINATED);
      }
    },
    STOPPED {
      @Override
      protected Set<State> getAllowed() {
        return EnumSet.of(RUNNING, FAILED, TERMINATED);
      }
    },
    FAILED {
      @Override
      protected Set<State> getAllowed() {
        return EnumSet.of(TERMINATED);
      }
    },
    TERMINATED {
      @Override
      protected Set<State> getAllowed() {
        return EnumSet.noneOf(State.class);
      }
    };

    protected abstract Set<State> getAllowed();

    public State next(State next) {
      Set<State> allowed = getAllowed();
      if (!allowed.contains(next)) {
        logger.error("Unable to change state from {} to {} (allowed: {})", this, next, allowed);
        throw new IllegalArgumentException();
      }
      return next;
    }
  }
}
