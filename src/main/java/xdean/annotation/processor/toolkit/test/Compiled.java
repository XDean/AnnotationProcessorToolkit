package xdean.annotation.processor.toolkit.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.processing.Processor;

@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface Compiled {
  String[] sources();

  Class<? extends Processor>[] processors() default {};

  String[] options() default {};
}
