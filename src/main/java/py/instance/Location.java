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

package py.instance;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.Serializable;
import org.apache.commons.lang3.Validate;
import py.common.FrequentlyUsedStrings;

public class Location implements Serializable {
  private static final long serialVersionUID = 0l;
  private static final String REGION_FIELD_NAME = "r";
  private static final String DC_FIELD_NAME = "d";
  private static final String RACK_FIELD_NAME = "c";
  private static final String HOST_FIELD_NAME = "h";
  private static final String LOCATION_FIELD_NAME = "l";

  private String region;
  private String dc;
  private String rack;
  private String host;

  public Location(Location other) {
    this.region = other.region;
    this.dc = other.dc;
    this.rack = other.rack;
    this.host = other.host;
  }

  public Location(String region, String dc) {
    this(region, dc, null, null);
  }

  public Location(String region, String dc, String rack, String host) {
    if (region == null || region.isEmpty()) {
      throw new IllegalArgumentException("Region can't be null");
    }
    if (dc == null || dc.isEmpty()) {
      throw new IllegalArgumentException("DC can't be null");
    }
    this.region = FrequentlyUsedStrings.get(region);
    this.dc = FrequentlyUsedStrings.get(dc);

    this.rack = rack == null ? null : new String(rack);
    this.host = host == null ? null : new String(host);
  }

  public static Location fromString(String locationStr) {
    if (locationStr == null) {
      return null;
    }

    String region = null;
    String dc = null;
    String rack = null;
    String host = null;

    String[] splittedString = locationStr.split("\\;");
    Validate.isTrue(splittedString.length > 0);

    for (String field : splittedString) {
      String [] fieldNameValue = field.split("\\=");

      Validate.isTrue(fieldNameValue.length == 2);
      Validate.isTrue(REGION_FIELD_NAME.equals(fieldNameValue[0])
          || DC_FIELD_NAME.equals(fieldNameValue[0])
          || RACK_FIELD_NAME.equals(fieldNameValue[0])
          || HOST_FIELD_NAME.equals(fieldNameValue[0]));

      if (region == null && REGION_FIELD_NAME.equals(fieldNameValue[0])) {
        region = fieldNameValue[1];
      } else if (dc == null && DC_FIELD_NAME.equals(fieldNameValue[0])) {
        dc = fieldNameValue[1];
      } else if (rack == null && RACK_FIELD_NAME.equals(fieldNameValue[0])) {
        rack = fieldNameValue[1];
      } else if (host == null && HOST_FIELD_NAME.equals(fieldNameValue[0])) {
        host = fieldNameValue[1];
      } else if (REGION_FIELD_NAME.equals(fieldNameValue[0])
          || DC_FIELD_NAME.equals(fieldNameValue[0])
          || RACK_FIELD_NAME.equals(fieldNameValue[0])
          || HOST_FIELD_NAME.equals(fieldNameValue[0])) {
        throw new IllegalArgumentException("Duplicate field: " + locationStr);
      }
    }
    return new Location(region, dc, rack, host);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(32);
    sb.append(REGION_FIELD_NAME).append("=").append(region).append(";");
    sb.append(DC_FIELD_NAME).append("=").append(dc);
    if (rack != null) {
      sb.append(";").append(RACK_FIELD_NAME).append("=").append(rack);
    }
    if (host != null) {
      sb.append(";").append(HOST_FIELD_NAME).append("=").append(host);
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((region == null) ? 0 : region.hashCode());
    result = prime * result + ((dc == null) ? 0 : dc.hashCode());
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + ((rack == null) ? 0 : rack.hashCode());
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
    Location other = (Location) obj;
    if (region == null) {
      if (other.region != null) {
        return false;
      }
    } else if (!region.equals(other.region)) {
      return false;
    }
    if (dc == null) {
      if (other.dc != null) {
        return false;
      }
    } else if (!dc.equals(other.dc)) {
      return false;
    }
    if (host == null) {
      if (other.host != null) {
        return false;
      }
    } else if (!host.equals(other.host)) {
      return false;
    }
    if (rack == null) {
      if (other.rack != null) {
        return false;
      }
    } else if (!rack.equals(other.rack)) {
      return false;
    }
    return true;
  }

  public String getHost() {
    return this.host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getDc() {
    return this.dc;
  }

  public void setDc(String dc) {
    this.dc = dc;
  }

  public String getRack() {
    return this.rack;
  }

  public void setRack(String rack) {
    this.rack = rack;
  }

  public String getRegion() {
    return this.region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public static class LocationDeserializer extends StdDeserializer<Location> {
    private static final long serialVersionUID = 913054104891080L;

    protected LocationDeserializer() {
      super(Location.class);
    }

    @Override
    public Location deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
      JsonNode node = jp.getCodec().readTree(jp);
      JsonNode locationNode = node.get(LOCATION_FIELD_NAME);
      if (locationNode == null) {
        throw new IOException("Can't find location field");
      }

      String locationString = locationNode.asText();
      if (locationString == null) {
        throw new IOException("location can't be empty");
      }

      return Location.fromString(locationString);
    }
  }

  public static class LocationSerializer extends StdSerializer<Location> {
    public LocationSerializer() {
      super(Location.class, true);
    }

    @Override
    public void serialize(Location value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException,
        JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeStringField(LOCATION_FIELD_NAME, value.toString());
      jgen.writeEndObject();
    }
  }
}
