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
