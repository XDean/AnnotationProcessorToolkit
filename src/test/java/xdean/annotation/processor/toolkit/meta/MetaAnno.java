package xdean.annotation.processor.toolkit.meta;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import xdean.annotation.processor.toolkit.annotation.Meta;

@Meta
@Retention(CLASS)
@Target({ TYPE, METHOD })
public @interface MetaAnno {

}
