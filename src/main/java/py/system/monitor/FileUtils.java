/*
    Copyright 2009 ThoughtWorks, Inc. All rights reserved.

    Redistribution and use in source and binary forms, with or without modification, are
    permitted provided that the following conditions are met:

       1. Redistributions of source code must retain the above copyright notice, this list of
          conditions and the following disclaimer.

       2. Redistributions in binary form must reproduce the above copyright notice, this list
          of conditions and the following disclaimer in the documentation and/or other materials
          provided with the distribution.

    THIS SOFTWARE IS PROVIDED BY THOUGHTWORKS, INC. ``AS IS'' AND ANY EXPRESS OR IMPLIED
    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
    FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THOUGHTWORKS, INC. OR
    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

    The views and conclusions contained in the software and documentation are those of the
    authors and should not be interpreted as representing official policies, either expressed
    or implied, of ThoughtWorks, Inc.
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

    /**
     * If you're using an operating system that supports the proc filesystem,
     * this returns a list of all processes by reading the directories under
     * /proc
     *
     * @return An array of the ids of all processes running on the OS.
     */
    public String[] pidsFromProcFilesystem() {
        return new File("/proc").list(FileUtils.PROCESS_DIRECTORY_FILTER);
    }

    /**
     * Given a filename, reads the entire file into a string.
     *
     * @param fileName The path of the filename to read. Should be absolute.
     * @return A string containing the entire contents of the file
     * @throws IOException If there's an IO exception while trying to read the file
     */
    public String slurp(String fileName) throws IOException {
        return slurpFromInputStream(new FileInputStream(fileName));
    }

    /**
     * Given a filename, reads the entire file into a byte array.
     *
     * @param fileName The path of the filename to read. Should be absolute.
     * @return A byte array containing the entire contents of the file
     * @throws IOException If there's an IO exception while trying to read the file
     */
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

    /**
     * Given an InputStream, reads the entire file into a string.
     *
     * @param stream The InputStream representing the file to read
     * @return A string containing the entire contents of the input stream
     * @throws IOException If there's an IO exception while trying to read the input stream
     */
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

    /**
     * Runs a regular expression on a file, and returns the first match.
     *
     * @param pattern The regular expression to use.
     * @param filename The path of the filename to match against. Should be absolute.
     * @return The first match found. Null if no matches.
     */
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
