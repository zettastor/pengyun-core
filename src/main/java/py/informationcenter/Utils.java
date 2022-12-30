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

package py.informationcenter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.common.struct.EndPointParser;
import py.instance.InstanceId;

public class Utils {
  public static final long defaultDomainId = -10000L;
  public static final String defaultDomainName = "DefaultDomain";
  public static final String defaultDomainDescription = "Default domain, contains all available "
      + "datanodes";
  public static final String SPLIT_CHARACTER_WITH_ENTRIES = "#";
  public static final String SPLIT_CHARACTER_WITH_KEY_AND_VALUE = "@";
  public static final String MOVE_VOLUME_APPEND_STRING_FOR_ORIGINAL_VOLUME = "_offline-move-volume";
  private static final Logger logger = LoggerFactory.getLogger(Utils.class);

  public static int convertNanosecondToSecond(long nanosecond) {
    return (int) nanosecond / 1000000000;
  }

  public static int convertNanosecondToMilliSecond(long nanosecond) {
    return (int) nanosecond / 1000000;
  }

  public static long convertMillisecondToNanosecond(int millisecond) {
    return millisecond * 1000000;
  }

  public static String millsecondToString(long milliseconds) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    Date date = new Date();
    date.setTime(milliseconds);
    return dateFormat.format(date);
  }

  public static void showMeCallStack(String info) {
    try {
      throw new RuntimeException();
    } catch (Exception e) {
      logger.warn("{}: ", info, e);
    }
  }

  public static String bulidJsonStrFromObjectLong(Collection<Long> objects) {
    if (objects == null) {
      logger.warn("Don't have any object to convert");
    }
    JSONArray array = JSONArray.fromObject(objects);
    return array.toString();
  }

  public static String bulidJsonStrFromObject(Collection<InstanceId> objects) {
    if (objects == null) {
      logger.warn("Don't have any object to convert");
    }
    JSONArray array = JSONArray.fromObject(objects);
    return array.toString();
  }

  @Deprecated
  public static String bulidJsonStrFromObjectEps(Collection<EndPoint> objects) {
    if (objects == null || objects.isEmpty()) {
      logger.warn("Don't have any object to convert");
      return null;
    }
    JSONArray array = JSONArray.fromObject(objects);
    return array.toString();
  }

  @Deprecated
  public static Collection<EndPoint> parseObjecFromJsonStrEps(String jsonStr) {
    if (jsonStr == null) {
      logger.warn("jsonStr is null, do not need to process any more");
      return null;
    }
    ObjectMapper objectMapper = new ObjectMapper();
    TypeReference<List<EndPoint>> typeRef = new TypeReference<List<EndPoint>>() {
    };
    List<EndPoint> endPoiontList = null;
    try {
      endPoiontList = objectMapper.readValue(jsonStr, typeRef);
    } catch (Exception e) {
      logger.error("convert json string to list, caught an exception", e);
    }
    return endPoiontList;
  }

  public static String buildStrFromObjectEps(List<EndPoint> objects) {
    if (objects == null || objects.isEmpty()) {
      logger.warn("Don't have any object to convert");
      return null;
    }

    StringBuilder sb = new StringBuilder(objects.get(0).toString());
    for (int i = 1; i < objects.size(); i++) {
      sb.append(",");
      sb.append(objects.get(i).toString());
    }

    return sb.toString();
  }

  public static List<EndPoint> parseObjectFromStrEps(String str) {
    if (str == null) {
      logger.warn("str is null, do not need to process any more");
      return null;
    }

    String[] endpointStrs = str.split(",");
    List<EndPoint> endPointList = new ArrayList<>();
    for (int i = 0; i < endpointStrs.length; i++) {
      endPointList.add(EndPoint.fromString(endpointStrs[i]));
    }
    return endPointList;
  }

  public static Collection<InstanceId> parseObjecFromJsonStr(String jsonStr) {
    if (jsonStr == null) {
      logger.warn("jsonStr is null, do not need to process any more");
      return null;
    }
    ObjectMapper objectMapper = new ObjectMapper();
    TypeReference<List<InstanceId>> typeRef = new TypeReference<List<InstanceId>>() {
    };
    List<InstanceId> instanceList = null;
    try {
      instanceList = objectMapper.readValue(jsonStr, typeRef);
    } catch (Exception e) {
      logger.error("convert json string to list, caught an exception", e);
    }
    return instanceList;
  }

  public static Collection<Long> parseObjecLongFromJsonStr(String jsonStr) {
    if (jsonStr == null) {
      logger.warn("jsonStr is null, do not need to process any more");
      return null;
    }
    ObjectMapper objectMapper = new ObjectMapper();
    TypeReference<List<Long>> typeRef = new TypeReference<List<Long>>() {
    };
    List<Long> instanceList = null;
    try {
      instanceList = objectMapper.readValue(jsonStr, typeRef);
    } catch (Exception e) {
      logger.error("convert json string to list, caught an exception", e);
    }
    return instanceList;
  }

  public static <T> T jsonToObj(String str, Class<T> clazz) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    if (StringUtils.isBlank(str) || clazz == null) {
      return null;
    }
    try {
      return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
    } catch (Exception e) {
      logger.warn("Parse String:{} to Object:{} error", str, clazz, e);
      return null;
    }
  }

  public static String bulidStringFromMultiMap(Multimap<Long, Long> archivesInDataNode) {
    if (archivesInDataNode == null || archivesInDataNode.isEmpty()) {
      logger.warn("Don't have any object to convert");
    }
    StringBuilder datanodeIdAndarchiveIds = new StringBuilder();
    Set<Long> datanodeIds = new HashSet<Long>();
    datanodeIds.addAll(archivesInDataNode.keySet());
    boolean firstLoop = true;
    for (Long datanodeId : datanodeIds) {
      if (firstLoop) {
        firstLoop = false;
      } else {
        datanodeIdAndarchiveIds.append(SPLIT_CHARACTER_WITH_ENTRIES);
      }
      datanodeIdAndarchiveIds.append(datanodeId);
      datanodeIdAndarchiveIds.append(SPLIT_CHARACTER_WITH_KEY_AND_VALUE);
      Validate.notEmpty(archivesInDataNode.get(datanodeId));
      datanodeIdAndarchiveIds
          .append(bulidJsonStrFromObjectLong(archivesInDataNode.get(datanodeId)));
    }
    return datanodeIdAndarchiveIds.toString();
  }

  public static Multimap<Long, Long> parseObjecOfMultiMapFromJsonStr(String jsonStr) {
    if (jsonStr == null) {
      logger.warn("jsonStr is null, do not need to process any more");
      return null;
    }

    Multimap<Long, Long> multiMap = Multimaps
        .synchronizedSetMultimap(HashMultimap.<Long, Long>create());
    String[] stringArray = jsonStr.split(SPLIT_CHARACTER_WITH_ENTRIES);
    Validate.notEmpty(stringArray, "must contain element %s", jsonStr);
    for (int i = 0; i < stringArray.length; i++) {
      String keyAndValue = stringArray[i];
      String[] keyAndValueArray = keyAndValue.split(SPLIT_CHARACTER_WITH_KEY_AND_VALUE);

      Validate.isTrue(keyAndValueArray.length == 2, "must contain element %s", keyAndValue);
      String key = keyAndValueArray[0];
      Set<Long> valueSet = new HashSet<Long>();
      valueSet.addAll(parseObjecLongFromJsonStr(keyAndValueArray[1]));
      multiMap.putAll(Long.valueOf(key), valueSet);
    }
    return multiMap;
  }

  public static synchronized boolean available(String ip, int port) {
    Validate.notNull(ip, "Invalid ip null");
    Validate.notEmpty(ip, "Invalid empty ip");
    Validate.isTrue(EndPointParser.MIN_PORT <= port && port <= EndPointParser.MAX_PORT,
        "Invalid start port: " + port);

    Socket s = null;
    try {
      s = new Socket();
      s.bind(new InetSocketAddress(ip, port));
      return true;
    } catch (Throwable e) {
      logger.error("caught exception", e);
    } finally {
      if (s != null) {
        try {
          s.close();
        } catch (IOException e) {
          logger.warn("Caught an exception when close socket on {}:{}", ip, port);
        }
      }
    }

    return false;
  }

  public static long getByteSize(String size) {
    Pattern pattern = Pattern.compile("(\\d+)([k|K|m|M|g|G]|)");
    Matcher matcher = pattern.matcher(size);
    Validate.isTrue(matcher.matches());
    Validate.isTrue(matcher.groupCount() == 1 || matcher.groupCount() == 2);
    String value = matcher.group(1);
    String unit = "";
    if (matcher.groupCount() == 2) {
      unit = matcher.group(2);
    }

    long longValue = Long.valueOf(value);
    if (unit.isEmpty()) {
      return longValue;
    } else if (unit.compareToIgnoreCase("k") == 0) {
      return longValue * 1024;
    } else if (unit.compareToIgnoreCase("m") == 0) {
      return longValue * 1024 * 1024;
    } else if (unit.compareToIgnoreCase("g") == 0) {
      return longValue * 1024 * 1024 * 1024;
    } else {
      throw new RuntimeException();
    }
  }

  public static long generateUuid(long offset, long checksum) {
    int leftMoveCount = Long.numberOfLeadingZeros(offset) - 1;

    offset <<= leftMoveCount;
    long uuid = offset | checksum;
    return uuid;
  }

  public static long generateUuid(int highPartUud, int lowPartUuid) {
    long high = highPartUud;
    long low = lowPartUuid;

    high = high << Integer.SIZE;

    low = low & 0x00000000ffffffffL;

    return high | low;
  }

  public static long generateUuidWithByteBuffer(int highPartUud, int lowPartUuid) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putInt(highPartUud);
    buffer.putInt(lowPartUuid);
    buffer.clear();
    return buffer.getLong();
  }

  public static String serialize(Object obj) throws IOException {
    Validate.notNull(obj, "serialize failed! object is null");

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream;
    objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(obj);
    String buffer = byteArrayOutputStream.toString("ISO-8859-1");
    objectOutputStream.close();
    byteArrayOutputStream.close();
    return buffer;
  }

  public static Object deserialize(String str) throws IOException, ClassNotFoundException {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
        str.getBytes("ISO-8859-1"));
    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
    Object obj = objectInputStream.readObject();
    objectInputStream.close();
    byteArrayInputStream.close();
    return obj;
  }

  public static String getEncoding(String str) {
    String encode = "GB2312";
    try {
      if (str.equals(new String(str.getBytes(encode), encode))) {
        String s = encode;
        return s;
      }
    } catch (Exception exception) {
      logger.error("error message:{}", exception);
    }
    encode = "ISO-8859-1";
    try {
      if (str.equals(new String(str.getBytes(encode), encode))) {
        String s1 = encode;
        return s1;
      }
    } catch (Exception exception1) {
      logger.error("error message:{}", exception1);
    }
    encode = "UTF-8";
    try {
      if (str.equals(new String(str.getBytes(encode), encode))) {
        String s2 = encode;
        return s2;
      }
    } catch (Exception exception2) {
      logger.error("error message:{}", exception2);
    }
    encode = "GBK";
    try {
      if (str.equals(new String(str.getBytes(encode), encode))) {
        String s3 = encode;
        return s3;
      }
    } catch (Exception exception3) {
      logger.error("error message:{}", exception3);
    }
    return "";
  }
}
