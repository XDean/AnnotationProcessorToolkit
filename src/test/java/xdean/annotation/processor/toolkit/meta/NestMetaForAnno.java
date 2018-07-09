package xdean.annotation.processor.toolkit.meta;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Before;

import xdean.annotation.processor.toolkit.annotation.MetaFor;

@Retention(CLASS)
@Target({ TYPE, METHOD })
@MetaAnno
@MetaFor(Before.class)
public @interface NestMetaForAnno {

}
