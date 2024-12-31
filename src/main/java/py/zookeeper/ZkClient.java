
package py.zookeeper;

import java.util.List;

public interface ZkClient {
  public List<String> getFiles(String path) throws ZkException;

  public List<String> getFiles(String path, boolean watch) throws ZkException;

  public void monitor(String path) throws ZkException;

  public byte[] readData(String path) throws ZkException;

  public void writeData(String path, byte[] data) throws ZkException;

  public void createPath(String path) throws ZkException;

  public String createFile(String path, byte[] data, boolean ephemeral) throws ZkException;

  public void close();

  public void deleteFile(String path) throws ZkException;

  public boolean exist(String path) throws ZkException;
}
