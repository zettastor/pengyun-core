

package py.store;

public interface IntCommitter<PathT, DataT> {
  public void to(PathT path) throws Exception;

  public IntCommitter<PathT, DataT> inFormatOf(Class<DataT> clazz) throws Exception;
}
