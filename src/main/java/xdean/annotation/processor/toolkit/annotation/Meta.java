package xdean.annotation.processor.toolkit.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicate the annotation is meta-annotation
 * 
 * @author Dean Xu (XDean@github.com)
 *
 */
@Meta
@Documented
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface Meta {

}
