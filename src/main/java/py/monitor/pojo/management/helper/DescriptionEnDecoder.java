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

package py.monitor.pojo.management.helper;

import py.monitor.utils.RegExUtils;

public class DescriptionEnDecoder {
  private static final String REG_EX_RANGE = "range=[\\[]*[\\d][\\,][\\d][\\]]";
  private static final String REG_EX_MEASUREMENT = "unitOfMeasurement=[\\w]*";
  private static final String REG_EX_DESCRIPTION = "description=[\\w]*";
  private String range;
  private String unitOfMeasurement;
  private String description;

  public void formatBy(String description) {
    this.range = RegExUtils.matchString(description, REG_EX_RANGE).replaceAll("range=", "");
    this.unitOfMeasurement = RegExUtils.matchString(description, REG_EX_MEASUREMENT)
        .replaceAll("unitOfMeasurement=", "");
    this.description = RegExUtils.matchString(description, REG_EX_DESCRIPTION)
        .replaceAll("description=", "");
  }

  public String getRange() {
    return range;
  }

  public void setRange(String range) {
    this.range = range;
  }

  public String getUnitOfMeasurement() {
    return unitOfMeasurement;
  }

  public void setUnitOfMeasurement(String unitOfMeasurement) {
    this.unitOfMeasurement = unitOfMeasurement;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "DescriptionEnDecoder [range=" + range + ", unitOfMeasurement=" + unitOfMeasurement
        + ", description="
        + description + "]";
  }

}
