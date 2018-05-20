package xdean.annotation.processor.toolkit.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Meta
@Documented
@Retention(CLASS)
@Target(ANNOTATION_TYPE)
public @interface MetaFor {
  Class<? extends Annotation> value();
}
