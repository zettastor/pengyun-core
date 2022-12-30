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

package py.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * Helper class for configuring Hibernate and retrieving session factories.
 */
@Deprecated
public class HibernateUtil {
  /**
   * Basic Hibernate helper class, handles SessionFactory, Session and Transaction.
   *
   * <p>Uses a static initializer for the initial SessionFactory creation and holds Session and
   * Transactions in thread local variables. All exceptions are wrapped in an unchecked
   * InfrastructureException.
   */
  private static final Logger LOG = Logger.getLogger(HibernateUtil.class);
  private static final ThreadLocal<Session> threadSession = new ThreadLocal<Session>();
  private static final ThreadLocal<Transaction> threadTransaction = new ThreadLocal<Transaction>();
  private static final ThreadLocal<Interceptor> threadInterceptor = new ThreadLocal<Interceptor>();
  private static Configuration configuration;
  private static SessionFactory sessionFactory;

  // Create the initial SessionFactory from the default configuration
  // files
  static {
    try {
      configuration = new Configuration();
      sessionFactory = configuration.configure().buildSessionFactory();
    } catch (Throwable ex) {
      // We have to catch Throwable, otherwise we will miss
      // NoClassDefFoundError and other subclasses of Error
      LOG.error("Building SessionFactory failed.", ex);
      throw new ExceptionInInitializerError(ex);
    }
  }

  /**
   * Returns the SessionFactory used for this static class.
   *
   * @return SessionFactory
   */
  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * Returns the original Hibernate configuration.
   *
   * @return Configuration
   */
  public static Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Rebuild the SessionFactory with the static Configuration.
   */
  public static void rebuildSessionFactory() throws DbException {
    synchronized (sessionFactory) {
      try {
        sessionFactory = getConfiguration().buildSessionFactory();
      } catch (Exception ex) {
        throw new DbException(ex);
      }
    }
  }

  /**
   * Rebuild the SessionFactory with the given Hibernate Configuration.
   */
  public static void rebuildSessionFactory(Configuration cfg) throws DbException {
    synchronized (sessionFactory) {
      try {
        sessionFactory = cfg.buildSessionFactory();
        configuration = cfg;
      } catch (Exception ex) {
        throw new DbException(ex);
      }
    }
  }

  /**
   * Retrieves the current Session local to the thread. <p/> If no Session is open, opens a new
   * Session for the running thread.
   *
   * @return Session
   */
  public static Session getSession() throws DbException {
    Session s = threadSession.get();
    try {
      if (s == null) {
        LOG.debug("Opening new Session for this thread.");
        if (getInterceptor() != null) {
          LOG.debug("Using interceptor: " + getInterceptor().getClass());

          s = getSessionFactory().withOptions().interceptor(getInterceptor()).openSession();
        } else {
          s = getSessionFactory().openSession();
        }
        threadSession.set(s);
      }
    } catch (Exception ex) {
      throw new DbException(ex);
    }
    return s;
  }

  /**
   * Closes the Session local to the thread.
   */
  public static void closeSession() throws DbException {
    try {
      Session s = threadSession.get();
      threadSession.set(null);
      if (s != null && s.isOpen()) {
        LOG.debug("Closing Session of this thread.");
        s.close();
      }
    } catch (Exception ex) {
      throw new DbException(ex);
    }
  }

  /**
   * Start a new database transaction.
   */
  public static void beginTransaction() throws DbException {
    Transaction tx = threadTransaction.get();
    try {
      if (tx == null) {
        LOG.debug("Starting new database transaction in this thread.");
        tx = getSession().beginTransaction();
        threadTransaction.set(tx);
      }
    } catch (DbException ex) {
      throw new DbException(ex);
    }
  }

  /**
   * Reconnects a Hibernate Session to the current Thread.
   *
   * @param session The Hibernate Session to be reconnected.
   */
  public static void reconnect(Session session) throws DbException {
    throw new DbException(new Exception("not implements"));
  }

  /**
   * Disconnect and return Session from current Thread.
   *
   * @return Session the disconnected Session
   */
  public static Session disconnectSession() throws DbException {
    Session session = getSession();
    try {
      threadSession.set(null);
      if (session.isConnected() && session.isOpen()) {
        session.disconnect();
      }
    } catch (Exception ex) {
      throw new DbException(ex);
    }
    return session;
  }

  /**
   * Register a Hibernate interceptor with the current thread.
   *
   * <p>Every Session opened is opened with this interceptor after registration. Has no effect
   * if the current Session of the thread is already open, effective on next close()/getSession().
   */
  public static void registerInterceptor(Interceptor interceptor) {
    threadInterceptor.set(interceptor);
  }

  private static Interceptor getInterceptor() {
    Interceptor interceptor = threadInterceptor.get();
    return interceptor;
  }

  /**
   * Converts a list of (String, Integer) to a map with the string as a key to the int.
   */
  public static Map<String, Integer> genericListToStatMap(List<?> list) {
    Map<String, Integer> result = new HashMap<String, Integer>();

    for (Object obj : list) {
      Object[] objConverted = (Object[]) obj;
      String user = (String) objConverted[0];
      Integer count = (Integer) objConverted[1];
      result.put(user, count);
    }

    return result;
  }

}
