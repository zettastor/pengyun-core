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

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import java.util.regex.Pattern;

public enum PyMetricReporterFilter implements MetricFilter {
  Graphite, CSV, Ganglia;

  private String regex;
  private Pattern pattern;

  public void compilePattern(String regex) {
    this.regex = regex;
    this.pattern = Pattern.compile(regex);
  }

  public String getRegex() {
    return regex;
  }

  @Override
  public boolean matches(String name, Metric metric) {
    if (pattern == null) {
      return false;
    }

    return pattern.matcher(name).find();
  }

}
