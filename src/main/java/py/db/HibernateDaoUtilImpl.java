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

import java.io.Serializable;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unchecked")
@Transactional
public class HibernateDaoUtilImpl extends HibernateDaoSupport implements HibernateDaoUtil {
  private static final Log log = LogFactory.getLog(HibernateDaoUtilImpl.class);

  /*
   * (non-Javadoc)
   *
   * @see com.hengbridge.data.dao.HibernateDaoWrapper#getHTemplate()
   */
  @Override
  public HibernateTemplate gethtemplate() {
    return super.getHibernateTemplate();
  }

  // ���������������
  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#attachDirty(java.lang.Object)
   */
  @Override
  public int attachDirty(Object entity) throws Exception {
    try {
      this.getHibernateTemplate().saveOrUpdate(entity);
      return 1;
    } catch (Exception e) {
      throw new Exception();
    }
  }

  // ������������
  /*
   * (non-Javadoc)
   *
   * @see com.hengbridge.data.dao.HibernateDaoWrapper#save(java.lang.Object)
   */
  @Override
  public int save(Object entity) throws Exception {
    try {
      this.getHibernateTemplate().save(entity);
      return 1;
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return 0;
    }
  }

  // ������������
  /*
   * (non-Javadoc)
   *
   * @see com.hengbridge.data.dao.HibernateDaoWrapper#update(java.lang.Object)
   */
  @Override
  public int update(Object entity) {
    try {
      getHibernateTemplate().update(entity);
      return 1;
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return 0;
    }
  }

  // ������������
  /*
   * (non-Javadoc)
   *
   * @see com.hengbridge.data.dao.HibernateDaoWrapper#delete(java.lang.Object)
   */
  @Override
  public int delete(Object entity) {
    try {
      super.getHibernateTemplate().delete(entity);
      return 1;
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return 0;
    }
  }

  // ������������
  /*
   * (non-Javadoc)
   *
   * @see com.hengbridge.data.dao.HibernateDaoWrapper#delete(java.lang.String,
   * java.lang.String)
   */
  @Override
  public int delete(String tableName, String condition) {
    String hql = HqlHelper.buildDeleteHql(tableName, condition);
    try {
      return delSome(hql);
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return 0;
    }
  }

  @Override
  public int delSome(final String hql) throws DataAccessException {
    int ret = 0;
    Integer integer;
    HibernateTemplate ht = new HibernateTemplate(this.getSessionFactory());
    integer = (Integer) ht.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException {
        Query query = session.createQuery(hql);
        return query.executeUpdate();
      }
    });
    if (integer != null) {
      ret = integer.intValue();
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.hengbridge.data.dao.HibernateDaoWrapper#count(java.lang.String,
   * java.lang.String)
   */
  @Override
  public int count(String tableName, String condition) {
    String hql = HqlHelper.buildCountHql(tableName, condition);
    try {
      return count(hql);
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.hengbridge.data.dao.HibernateDaoWrapper#count(java.lang.String)
   */
  @Override
  public int count(final String hql) throws DataAccessException {
    int ret = 0;
    Long along;
    HibernateTemplate ht = new HibernateTemplate(this.getSessionFactory());
    along = (Long) ht.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException {
        Query query = session.createQuery(hql);
        return query.uniqueResult();
      }
    });
    if (along != null) {
      ret = along.intValue();
    }
    return ret;
  }

  // ������������������������������������num���(������num���null������������)
  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#queryList(java.lang.String,
   * java.lang.String, java.lang.Integer)
   */
  @Override
  public List queryList(String objName, String condition, Integer maxNum) {
    return queryList(null, objName, condition, maxNum);
  }

  @Override
  public List queryList(String items, String objName, String condition, Integer maxNum) {
    return queryList(items, objName, condition, null, maxNum);
  }

  @Override
  public List queryList(String items, String objName, String condition, String order,
      Integer maxNum) {
    String hql = HqlHelper.buildSelectHql(items, objName, condition, order);
    try {
      return queryList(hql, maxNum);
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#queryList(java.lang.String,
   * java.lang.Integer)
   */
  @Override
  public List queryList(final String hql, final Integer maxNum) throws DataAccessException {
    HibernateTemplate ht = new HibernateTemplate(this.getSessionFactory());
    return (List) ht.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException {
        Query query = session.createQuery(hql);
        if (maxNum != null && maxNum.intValue() > 0) {
          query.setMaxResults(maxNum);
        }
        List result = query.list();
        System.out.println("result size:" + result.size());
        return result;
      }
    });
  }

  // ������������
  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#queryPager(java.lang.String,
   * java.lang.String, java.lang.Integer, java.lang.Integer)
   */
  @Override
  public List queryPager(String objName, String condition, Integer curpageInt, Integer pageSize) {
    return queryPager(null, objName, condition, curpageInt, pageSize);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#queryPager(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
   */
  @Override
  public List queryPager(String items, String objName, String condition, Integer curpageInt,
      Integer pageSize) {
    String hql = HqlHelper.buildSelectHql(items, objName, condition, null);
    try {
      return queryPager(hql, curpageInt, pageSize);
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#queryPager(java.lang.String,
   * java.lang.Integer, java.lang.Integer)
   */
  @Override
  public List queryPager(final String hql, final Integer curpageInt, final Integer pageSize) {
    HibernateTemplate ht = new HibernateTemplate(this.getSessionFactory());
    return (List) ht.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException {
        Query query = session.createQuery(hql);
        query.setFirstResult(pageSize * (curpageInt - 1));
        query.setMaxResults(pageSize);
        List result = query.list();
        return result;
      }
    });
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#queryPager(java.lang.String,
   * java.lang.Integer, java.lang.Integer)
   */
  @Override
  public List queryPagerByPosition(final String hql, final Integer startPosition,
      final Integer pageSize) {
    HibernateTemplate ht = new HibernateTemplate(this.getSessionFactory());
    return (List) ht.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException {
        Query query = session.createQuery(hql);
        query.setFirstResult(startPosition);
        query.setMaxResults(pageSize);
        List result = query.list();
        return result;
      }
    });
  }

  // ������������������������
  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#findObject(java.lang.String,
   * java.lang.String)
   */
  @Override
  public Object findObject(String objName, String condition) {
    return findObject(null, objName, condition);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#findObject(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public Object findObject(String items, String objName, String condition) {
    String hql = HqlHelper.buildSelectHql(items, objName, condition, null);

    try {
      return findObject(hql);
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.hengbridge.data.dao.HibernateDaoWrapper#findObject(java.lang.String)
   */
  @Override
  public Object findObject(final String hql) throws DataAccessException {
    HibernateTemplate ht = new HibernateTemplate(this.getSessionFactory());
    return ht.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException {
        Object result;
        try {
          Query query = session.createQuery(hql);
          result = query.uniqueResult();
        } catch (RuntimeException e) {
          result = null;
          log.debug("findObject Catch Exception :" + e);
          e.printStackTrace();
        }
        return result;
      }
    });
  }

  @Override
  public Object get(Class entityClass, Serializable id) {
    try {
      return this.getHibernateTemplate().get(entityClass, id);
    } catch (Exception e) {
      log.error("Catch Exception :" + e);
      return null;
    }
  }
}
