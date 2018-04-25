package xdean.annotation.processor.toolkit.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.lang.model.SourceVersion;

@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface Compile {
  String[] sources();

  Class<? extends Annotation>[] annotations() default {};

  SourceVersion version() default SourceVersion.RELEASE_8;
}
