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
