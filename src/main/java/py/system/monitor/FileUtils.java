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

package py.system.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
  private static final Pattern PROC_DIR_PATTERN = Pattern.compile("([\\d]*)");

  private static final FilenameFilter PROCESS_DIRECTORY_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      File fileToTest = new File(dir, name);
      return fileToTest.isDirectory() && PROC_DIR_PATTERN.matcher(name).matches();
    }
  };

  public String[] pidsFromProcFilesystem() {
    return new File("/proc").list(FileUtils.PROCESS_DIRECTORY_FILTER);
  }

  public String slurp(String fileName) throws IOException {
    return slurpFromInputStream(new FileInputStream(fileName));
  }

  public byte[] slurpToByteArray(String fileName) throws IOException {
    File fileToRead = new File(fileName);
    byte[] contents = new byte[(int) fileToRead.length()];
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(fileToRead);
      inputStream.read(contents);
      return contents;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  public String slurpFromInputStream(InputStream stream) throws IOException {
    if (stream == null) {
      return null;
    }
    StringWriter sw = new StringWriter();
    String line;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      while ((line = reader.readLine()) != null) {
        sw.write(line);
        sw.write('\n');
      }
    } finally {
      stream.close();
    }
    return sw.toString();
  }

  public String runRegexOnFile(Pattern pattern, String filename) {
    try {
      final String file = slurp(filename);
      Matcher matcher = pattern.matcher(file);
      matcher.find();
      final String firstMatch = matcher.group(1);
      if (firstMatch != null && firstMatch.length() > 0) {
        return firstMatch;
      }
    } catch (IOException e) {
      // do nothing
    }
    return null;
  }

  public String realPath(String link) throws IOException {
    return new File(link).toPath().toRealPath().toString();
  }

  public int numberOfSubFiles(String directory) throws IOException {
    String[] subFiles = null;

    subFiles = new File(directory).list();
    if (subFiles == null) {
      throw new IOException("No such directory " + directory);
    } else {
      return subFiles.length;
    }
  }
}
