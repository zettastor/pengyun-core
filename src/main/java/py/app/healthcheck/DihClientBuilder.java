package py.app.healthcheck;

import py.periodic.UnableToStartException;

public interface DihClientBuilder {

  void startDihClientBuild() throws UnableToStartException;

  void stopDihClientBuild();

}
