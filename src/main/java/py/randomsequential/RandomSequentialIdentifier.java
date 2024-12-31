

package py.randomsequential;

public interface RandomSequentialIdentifier {
  boolean updateLastOffsetAndIsSequential(long offset, int length);

}
