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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;
import py.informationcenter.Utils;
import py.instance.InstanceId;

public class ParseObjectAndBuildBackTest extends TestBase {
  @Test
  public void testParseJsonStrFromInstanceIdList() {
    List<InstanceId> datanodeIdList = new ArrayList<>();
    InstanceId id1 = new InstanceId(10001);
    InstanceId id2 = new InstanceId(10002);
    InstanceId id3 = new InstanceId(10003);
    datanodeIdList.add(id1);
    datanodeIdList.add(id2);
    datanodeIdList.add(id3);

    String trans = Utils.bulidJsonStrFromObject(datanodeIdList);

    Set<InstanceId> transList = new HashSet<>();
    transList.addAll(Utils.parseObjecFromJsonStr(trans));
    assertTrue(transList.size() == datanodeIdList.size());
    for (InstanceId datanodeId : datanodeIdList) {
      assertTrue(transList.contains(datanodeId));
    }
  }

  @Test
  public void testParseJsonStrFromLongList() {
    List<Long> datanodeIdList = new ArrayList<>();
    Long id1 = 10001L;
    Long id2 = 10002L;
    Long id3 = 10003L;
    datanodeIdList.add(id1);
    datanodeIdList.add(id2);
    datanodeIdList.add(id3);

    String trans = Utils.bulidJsonStrFromObjectLong(datanodeIdList);

    Set<Long> transList = new HashSet<>();
    transList.addAll(Utils.parseObjecLongFromJsonStr(trans));
    assertTrue(transList.size() == datanodeIdList.size());
    for (Long datanodeId : datanodeIdList) {
      assertTrue(transList.contains(datanodeId));
    }
  }

  @Test
  public void testTransformMultiMap() {
    List<Long> datanodeIdList = new ArrayList<>();
    List<Long> archiveIdList = new ArrayList<>();
    Long id1 = 10001L;
    Long archiveId11 = 1234L;
    datanodeIdList.add(id1);
    archiveIdList.add(archiveId11);

    Long id2 = 10002L;
    Long archiveId21 = 2234L;
    Long archiveId22 = 2235L;
    datanodeIdList.add(id2);
    archiveIdList.add(archiveId21);
    archiveIdList.add(archiveId22);

    Long id3 = 10003L;
    Long archiveId31 = 3234L;
    Long archiveId32 = 3235L;
    datanodeIdList.add(id3);
    archiveIdList.add(archiveId31);
    archiveIdList.add(archiveId32);
    Long archiveId33 = 3236L;
    archiveIdList.add(archiveId33);

    Multimap<Long, Long> archivesInDataNode = Multimaps
        .synchronizedSetMultimap(HashMultimap.<Long, Long>create());
    archivesInDataNode.put(id1, archiveId11);

    archivesInDataNode.put(id2, archiveId21);
    archivesInDataNode.put(id2, archiveId22);

    archivesInDataNode.put(id3, archiveId31);
    archivesInDataNode.put(id3, archiveId32);
    archivesInDataNode.put(id3, archiveId33);

    String trans = Utils.bulidStringFromMultiMap(archivesInDataNode);

    Multimap<Long, Long> newMap = Multimaps
        .synchronizedSetMultimap(HashMultimap.<Long, Long>create());

    newMap = Utils.parseObjecOfMultiMapFromJsonStr(trans);

    assertTrue(newMap.size() == archiveIdList.size());

    for (Entry<Long, Long> entry : archivesInDataNode.entries()) {
      assertTrue(newMap.containsEntry(entry.getKey(), entry.getValue()));
    }
  }

  @Test
  public void testStringSplit() {
    String string1 = "1111";
    String string2 = "1111@2222";
    String[] stringArray1 = string1.split("@");
    assertTrue(stringArray1.length == 1);
    String[] stringArray2 = string2.split("@");
    assertTrue(stringArray2.length == 2);
  }
}
