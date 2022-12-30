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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.informationcenter.Utils;

public class TransitionStringwithEndPointTest extends TestBase {
  public static Logger logger = LoggerFactory.getLogger(TransitionStringwithEndPointTest.class);

  public static EndPoint buildEndPoint(String hostName, int port) {
    EndPoint endPoint = new EndPoint();
    endPoint.setHostName(hostName);
    endPoint.setPort(port);
    return endPoint;
  }

  @Test
  public void testTransition() {
    Collection<EndPoint> endPoiontCollection = new ArrayList<>();
    endPoiontCollection.add(buildEndPoint("10.0.1.156", 10002));
    endPoiontCollection.add(buildEndPoint("10.2.2.255", 10086));
    endPoiontCollection.add(buildEndPoint("255.255.0.255", 8080));
    String endPointJsonStr = Utils.bulidJsonStrFromObjectEps(endPoiontCollection);
    Collection<EndPoint> endPoiontCollectionAfterTransition = Utils
        .parseObjecFromJsonStrEps(endPointJsonStr);
    assertTrue(endPoiontCollection.equals(endPoiontCollectionAfterTransition));
  }

  @Test
  public void testTransitionEpsBySplit() {
    List<EndPoint> endPointList = new ArrayList<>();
    endPointList.add(buildEndPoint("10.0.1.156", 10002));
    String endPointStr = Utils.buildStrFromObjectEps(endPointList);
    List<EndPoint> endPoiontListAfterParse = Utils.parseObjectFromStrEps(endPointStr);
    assertTrue(endPointList.equals(endPoiontListAfterParse));

    endPointList.add(buildEndPoint("10.2.2.255", 10086));
    endPointList.add(buildEndPoint("255.255.0.255", 8080));
    endPointStr = Utils.buildStrFromObjectEps(endPointList);
    endPoiontListAfterParse = Utils.parseObjectFromStrEps(endPointStr);
    assertTrue(endPointList.equals(endPoiontListAfterParse));
  }

}
