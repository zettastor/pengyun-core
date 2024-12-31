

package py.app.context;

import py.instance.Group;

public interface GroupStore {
  public Group getGroup();

  public void persistGroup(Group group) throws Exception;

  /**
   * once after deploy, group id was set, can not modify it any more.
   */
  public boolean canUpdateGroup(Group newGroup) throws Exception;

  public static class NullGroupStore implements GroupStore {
    @Override
    public void persistGroup(Group group) {
     
    }

    @Override
    public Group getGroup() {
      return null;
    }

    @Override
    public boolean canUpdateGroup(Group newGroup) {
      return false;
    }
  }
}
