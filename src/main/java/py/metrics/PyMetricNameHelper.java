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

package py.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.exception.FormatIncorrectException;
import py.monitor.exception.UnsupportedIdentifierTypeException;
import py.monitor.jmx.server.ResourceType;
import py.monitor.utils.RegExUtils;

public class PyMetricNameHelper<IdentifierDataT> implements java.io.Serializable {
  private static final Logger logger = LoggerFactory.getLogger(PyMetricNameHelper.class);
  private static final long serialVersionUID = 699962755338453915L;
  private static final String METRICS_IDENTIFIER_PREFIX = "__<<TYPE[";
  private static final String REGEX_METRICS_IDENTIFIER_PREFIX = "__<<TYPE\\[";
  private static final String METRICS_IDENTIFIER_POSTFIX = "]>>";
  private static final String REGEX_METRICS_IDENTIFIER_POSTFIX = "\\]>>";
  private static final String SEPERATOR = "] ID[";
  private static final String REGEX_SEPERATOR = "\\] ID\\[";
  private static final String NAME_SEPERATOR = ".";
  protected Class<IdentifierDataT> clazz;
  private String name;
  private ResourceType type;
  private IdentifierDataT identifier;

  protected PyMetricNameHelper(String name, ResourceType type, IdentifierDataT identifier) {
    this.name = name;
    this.type = type;
    this.identifier = identifier;
  }

  @SuppressWarnings("unchecked")
  public PyMetricNameHelper(Class<IdentifierDataT> identifierType, String serializedIdentifier)
      throws FormatIncorrectException, UnsupportedIdentifierTypeException {
    if (serializedIdentifier == null) {
      logger.error("serialized metric name is not in correct format");
      throw new FormatIncorrectException();
    }

    String remainderString = serializedIdentifier;

    String regex =
        "[\\w.*_#$%@!~&.<=\\{\\}\\?\\-\\^\\(\\)\\[\\]\\:]*" + REGEX_METRICS_IDENTIFIER_PREFIX;
    String subStr = RegExUtils.matchString(remainderString, regex);
    if (subStr == "") {
      this.name = serializedIdentifier;
      this.type = ResourceType.NONE;
      if (identifierType.getName().equals(Long.class.getName())) {
        identifier = (IdentifierDataT) Long.valueOf(0L);
      } else if (identifierType.getName().equals(String.class.getName())) {
        identifier = (IdentifierDataT) "0";
      } else {
        logger.error("Unsupported identifier type");
        throw new UnsupportedIdentifierTypeException();
      }
    } else {
      remainderString = remainderString.substring(subStr.length(), remainderString.length());
      logger.trace("Current remainder string is {}", remainderString);
      this.name = subStr.substring(0,
          subStr.length() - METRICS_IDENTIFIER_PREFIX.length() - NAME_SEPERATOR.length());

      regex = ResourceType.getRegex() + REGEX_SEPERATOR;
      subStr = RegExUtils.matchString(remainderString, regex);
      if (subStr != "") {
        remainderString = remainderString.substring(subStr.length(), remainderString.length());
        logger.trace("Current remainder string is {}", remainderString);
        this.type = ResourceType.valueOf(subStr.substring(0, subStr.length() - SEPERATOR.length()));
      }

      regex =
          "[\\w.*_#$%@!~&.<=\\{\\}\\?\\-\\^\\(\\)\\[\\]\\:]+" + REGEX_METRICS_IDENTIFIER_POSTFIX;
      subStr = RegExUtils.matchString(remainderString, regex);
      if (subStr == "") {
        logger.error("serialized identifier is not in correct format");
        throw new FormatIncorrectException();
      } else {
        remainderString = remainderString.substring(subStr.length(), remainderString.length());
        logger.trace("Current remainder string is {}, subStr is {}", remainderString, subStr);
        String identifierStr = subStr
            .substring(0, subStr.length() - METRICS_IDENTIFIER_POSTFIX.length());

        if (identifierType.getName().equals(Long.class.getName())) {
          identifier = (IdentifierDataT) Long.valueOf(identifierStr);
        } else if (identifierType.getName().equals(String.class.getName())) {
          identifier = (IdentifierDataT) identifierStr;
        } else {
          logger.error("Unsupported identifier type");
          throw new UnsupportedIdentifierTypeException();
        }
      }
    }
  }

  public static String getMetricsIdentifierPrefix() {
    return METRICS_IDENTIFIER_PREFIX;
  }

  public static String getMetricsIdentifierPostfix() {
    return METRICS_IDENTIFIER_POSTFIX;
  }

  public static String getSeperator() {
    return SEPERATOR;
  }

  public static String getNameSeperator() {
    return NAME_SEPERATOR;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ResourceType getType() {
    return type;
  }

  public void setType(ResourceType type) {
    this.type = type;
  }

  public IdentifierDataT getIdentifier() {
    return identifier;
  }

  public void setIdentifier(IdentifierDataT identifier) {
    this.identifier = identifier;
  }

  public String generate() throws UnsupportedIdentifierTypeException {
    String returnString = "";
    if ((type == null && identifier != null) || (type != null && identifier == null)) {
      logger.error("Unsupported identifier");
      throw new UnsupportedIdentifierTypeException();
    } else if (type == null && identifier == null) {
      logger.warn("This metrics is not assigned with a specified resouce");
      returnString = this.name;
    } else {
      returnString =
          this.name + NAME_SEPERATOR + METRICS_IDENTIFIER_PREFIX + type + SEPERATOR + identifier
              + METRICS_IDENTIFIER_POSTFIX;
    }
    return returnString;
  }

  public String getAsRepositoryName() throws UnsupportedIdentifierTypeException {
    if ((type == null && identifier != null) || (type != null && identifier == null)) {
      logger.error("Unsupported identifier");
      throw new UnsupportedIdentifierTypeException();
    } else {
      return name;
    }
  }

  @Override
  public String toString() {
    return "ResourceIdentifier [name=" + name + ", type=" + type + ", identifier=" + identifier
        + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    PyMetricNameHelper other = (PyMetricNameHelper) obj;
    if (identifier == null) {
      if (other.identifier != null) {
        return false;
      }
    } else if (!identifier.equals(other.identifier)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }
}
