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
