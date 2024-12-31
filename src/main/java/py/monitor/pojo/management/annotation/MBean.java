

package py.monitor.pojo.management.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import py.monitor.jmx.server.ResourceType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface MBean {
  String objectName() default "";

  ResourceType resourceType() default ResourceType.NONE;

  AutomaticType[] automatic() default {};

  public static enum AutomaticType {
    ATTRIBUTE, OPERATION
  }
}
