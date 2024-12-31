

package py.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
  public int times() default 3;

  public int period() default 1;

  public Class<? extends Throwable>[] when() default None.class;

  static class None extends Throwable {
    private static final long serialVersionUID = 1L;

    private None() {
    }
  }
}
