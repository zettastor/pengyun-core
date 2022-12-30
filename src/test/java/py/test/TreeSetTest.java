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

package py.test;

import static org.junit.Assert.assertTrue;

import java.util.TreeSet;
import org.junit.Test;

public class TreeSetTest extends TestBase {
  public TreeSetTest() throws Exception {
    super.init();
  }

  @Test
  public void add() throws Exception {
    TreeSet<Person> sortContexts = new TreeSet<Person>();
    assertTrue(sortContexts.add(new Person("test1", 1)));
    assertTrue(!sortContexts.add(new Person("test2", 1)));
  }

  class Person implements Comparable<Person> {
    public int age;
    public String name;

    public Person(String name, int age) {
      this.age = age;
      this.name = name;
    }

    @Override
    public int compareTo(Person o) {
      if (age > o.age) {
        return 1;
      } else if (age == o.age) {
        return 0;
      } else {
        return -1;
      }
    }

    public boolean equals(Object obj) {
      if (obj instanceof Person) {
        Person r = (Person) obj;
        if (this == r) {
          return true;
        }
      }
      return false;
    }
  }
}
