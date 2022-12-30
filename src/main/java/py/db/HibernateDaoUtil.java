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
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

@SuppressWarnings("unchecked")
public interface HibernateDaoUtil {
  HibernateTemplate gethtemplate();

  // update or insert
  public int attachDirty(Object entity) throws Exception;

  //insert
  int save(Object entity) throws Exception;

  //update
  int update(Object entity);

  // delete
  int delete(Object entity);

  //delete under some condition
  int delete(String objName, String condition);

  int delSome(final String hql) throws DataAccessException;

  //count according to some condition
  int count(String objName, String condition);

  int count(final String hql) throws DataAccessException;

  //Conditional query, return a list, if num is null, return all
  List queryList(String objName, String condition, Integer maxNum);

  List queryList(String items, String objName, String condition,
      Integer maxNum);

  List queryList(String items, String objName, String condition, String order, Integer maxNum);

  List queryList(final String hql, final Integer maxNum)
      throws DataAccessException;

  List queryPager(String objName, String condition, Integer curpageInt,
      Integer pageSize);

  List queryPager(String items, String objName, String condition,
      Integer curpageInt, Integer pageSize);

  List queryPager(final String hql, final Integer curpageInt,
      final Integer pageSize);

  List queryPagerByPosition(final String hql, final Integer startPosition,
      final Integer pageSize);

  //find an object according to a condition
  Object findObject(String objName, String condition);

  Object findObject(String items, String objName, String condition);

  Object findObject(final String hql) throws DataAccessException;

  Object get(Class entityClass, Serializable id);

}
