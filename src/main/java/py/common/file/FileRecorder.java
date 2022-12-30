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

package py.common.file;

import java.util.List;

/**
 * an interface for recording a str in a file.
 */
public interface FileRecorder {
  String ENCODING = "utf-8";

  boolean add(String record);

  boolean remove(String record);

  boolean fileExist();

  boolean contains(String record);

  boolean isEmpty();

  List<String> records();
}
