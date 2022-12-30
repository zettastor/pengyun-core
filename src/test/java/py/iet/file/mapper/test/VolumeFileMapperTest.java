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

package py.iet.file.mapper.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import py.iet.file.mapper.VolumeFileMapper;

public class VolumeFileMapperTest {
  private VolumeFileMapper volumeFileMapper = new VolumeFileMapper();

  @Before
  public void init() throws IOException {
    String filePath = "/tmp/VolumeFileMapperTest";

    volumeFileMapper.setFilePath(filePath);

    BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(filePath));
    bufferWriter.write("tid:6 name:iqn.test");
    bufferWriter.newLine();
    bufferWriter.write(
        "\t   lun:0 state:0 iotype:"
            + "fileio iomode:wt blocks:2097152 blocksize:512 path:/home/py_ops/test");
    bufferWriter.flush();
    bufferWriter.close();
  }

  @Test
  public void testVolumeFileMapper() {
    volumeFileMapper.load();
    assertTrue(volumeFileMapper.getVolumeList().size() == 1);
    assertTrue(volumeFileMapper.getVolumeList().get(0).getLunList().size() == 1);
    assertTrue(volumeFileMapper.getVolumeList().get(0).getTid() == 6);
    assertTrue(volumeFileMapper.getVolumeList().get(0).getTargetName().equals("iqn.test"));
    assertTrue(volumeFileMapper.getVolumeList().get(0).getLunList().get(0).getIndex() == 0);
    assertTrue(
        volumeFileMapper.getVolumeList().get(0).getLunList().get(0).getType().equals("fileio"));
    assertTrue(volumeFileMapper.getVolumeList().get(0).getLunList().get(0).getPath()
        .equals("/home/py_ops/test"));
  }

}
