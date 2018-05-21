# Meta-Annotation

Meta annotation is annotation for annotation. Java provides [several default meta-annotation](https://en.wikibooks.org/wiki/Java_Programming/Annotations/Meta-Annotations). In this framework, you can use `@Meta` and `AbstractMetaProcessor` to quick build `Processor` for meta-annotation. And use `@MetaFor` to add meta annotations on annotations that you can't modify.

- [Define meta-annotation](#define-a-meta-annotation)
- [Use meta-annotation](#use-meta-annotation)
  - [Direct use](#direct-use)
  - [Meta use](#meta-use)
- [Use `@MetaFor`](#metafor)
- [Write processor](#write-meta-annotation-processor)

# Define a meta-annotation

1. Add `@Meta` on your annotation.
2. Add `ANNOTATION_TYPE` into the `@Target`.
3. At least use `CLASS` retention.

```java
@Meta
@Retention(CLASS)
@Target({ANNOTATION_TYPE, METHOD, ...})
@interface MetaAnno{
  int value() default 0;
} 
```

# Use meta-annotation

### Direct Use

```java
@MetaAnno(1)
public void func(){
  ...
}
```

### Meta Use

```java
@MetaAnno(2)
@interface UseMetaAnno{
  ...
}
```

now every where use `@UseMetaAnno` implies `@MetaAnno(2)`.

# `@MetaFor`

Since some third party code is hard to modify. You can use `@MetaFor` to add meta-annotation on them.

```java
@MetaFor(org.junit.Test.class)
@CheckMethod(argCount=0)
public @interface JUnitTestMeta{
}
```

now it is equals to add `@CheckMethod` on `@Test`

# Write meta-annotation processor

## Create class

1. Extends `AbstractMetaProcessor` with generic
2. Annotated with `@SupportedMetaAnnotation`

```java
@SupportedMetaAnnotation(MetaAnno.class)
public class MetaAnnoProcessor extends AbstractMetaProcessor<MetaAnno> {
}
```

## Implements methods

`AbstractMetaProcessor` has an abstract method and an optional method for overridden.

### Process Annotation

```java
protected abstract void process(RoundEnvironment env, T t, @CheckForNull AnnotationMirror mid, Element element) throws AssertException
```

1. environment
2. the meta-annotation object, direct or indirect
3. the metaed-annotation mirror. If the meta-annotation is annotated directly, it will be null.
4. the annotated element

For samples at [Use meta-annotation](#use-meta-annotation)

1. for usage 1, it will call `process(env, @MetaAnno(1), null, 'func' method element)`
2. for usage 2, it will call `process(env, @MetaAnno(2), @UseMetaAnno, the element annotated by @UseMetaAnno)`

### Process Meta Annotation

```java
protected void processMeta(RoundEnvironment env, T t, Element element) throws AssertException
```

1. environment
2. the meta-annotation object on the element directly
3. the annotated element

For samples at [Use meta-annotation](#use-meta-annotation)

1. for usage 1, it will call `processMeta(env, @MetaAnno(1), 'func' method element)`
2. for usage 2, it will call `processMeta(env, @MetaAnno(2), 'UseMetaAnno' type element)`

This method is usually use to check the meta-annotation itself is well defined.
 