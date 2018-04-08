package xdean.annotation.processor.toolkit.getClass;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Anno {
  Class<?> classValue() default Anno.class;

  Class<?>[] classArray() default {};
}
