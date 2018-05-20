package xdean.annotation.processor.toolkit.meta;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import xdean.annotation.processor.toolkit.AssertException;
import xdean.annotation.processor.toolkit.ElementUtil;
import xdean.annotation.processor.toolkit.NestCompileFile;
import xdean.annotation.processor.toolkit.XAbstractProcessor;
import xdean.annotation.processor.toolkit.annotation.Meta;
import xdean.annotation.processor.toolkit.annotation.MetaFor;
import xdean.annotation.processor.toolkit.annotation.SupportedMetaAnnotation;

public abstract class AbstractMetaProcessor<T extends Annotation> extends XAbstractProcessor {
  protected final Class<T> metaClass;
  protected final NestCompileFile metaFile;
  protected final Set<String> annotatedAnnotationNames;

  @SuppressWarnings("unchecked")
  public AbstractMetaProcessor() {
    SupportedMetaAnnotation meta = this.getClass().getAnnotation(SupportedMetaAnnotation.class);
    if (meta == null) {
      throw new Error("AbstractMetaProcessor must use with @SupportedMetaAnnotation.");
    } else if (!meta.value().isAnnotationPresent(Meta.class)) {
      throw new Error("AbstractMetaProcessor only can process Annotation with @Meta.");
    }
    metaClass = (Class<T>) meta.value();
    metaFile = new NestCompileFile(metaPath(metaClass));
    try {
      annotatedAnnotationNames = metaFile.readLines().collect(Collectors.toSet());
    } catch (IOException e) {
      throw new Error("Fail to read meta file.", e);
    }
  }

  @Override
  public boolean processActual(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws AssertException {
    if (roundEnv.processingOver()) {
      return false;
    }
    Map<Boolean, List<Element>> map = roundEnv.getElementsAnnotatedWith(metaClass)
        .stream()
        .collect(Collectors.groupingBy(e -> e.getKind() == ElementKind.ANNOTATION_TYPE));
    List<Element> annotatedElements = map.getOrDefault(false, Collections.emptyList());
    List<TypeElement> annotatedAnnotations = map.getOrDefault(true, Collections.emptyList()).stream().map(e -> (TypeElement) e)
        .collect(Collectors.toList());
    annotatedElements.forEach(e -> handleAssert(() -> {
      T anno = e.getAnnotation(metaClass);
      processMeta(roundEnv, anno, e);
      process(roundEnv, anno, null, e);
    }));
    annotatedAnnotations.forEach(te -> handleAssert(() -> {
      processMeta(roundEnv, te.getAnnotation(metaClass), te);
      T meta = te.getAnnotation(metaClass);
      TypeElement actual = handleMetaFor(te);
      TypeMirror actualType = actual.asType();
      roundEnv.getElementsAnnotatedWith(actual).forEach(
          e -> handleAssert(() -> process(roundEnv, meta, ElementUtil.getAnnotationMirror(e, actualType).get(), e)));
    }));
    annotatedAnnotationNames.stream().map(s -> elements.getTypeElement(s))
        .forEach(te -> {
          T meta = te.getAnnotation(metaClass);
          TypeElement actual = handleMetaFor(te);
          TypeMirror actualType = actual.asType();
          roundEnv.getElementsAnnotatedWith(actual).forEach(
              e -> handleAssert(() -> process(roundEnv, meta, ElementUtil.getAnnotationMirror(e, actualType).get(), e)));
        });
    writeMetaClasses(annotatedAnnotations);
    return false;
  }

  protected void writeMetaClasses(List<TypeElement> annotatedAnnotations) throws Error {
    try (PrintStream printer = metaFile.getPrintStream(filer)) {
      annotatedAnnotations.forEach(e -> printer.println(e.asType().toString()));
    } catch (IOException e) {
      throw new Error("Error to write meta file.", e);
    }
  }

  protected TypeElement handleMetaFor(TypeElement origin) {
    MetaFor metaFor = origin.getAnnotation(MetaFor.class);
    if (metaFor == null) {
      return origin;
    } else {
      return (TypeElement) ((DeclaredType) ElementUtil.getAnnotationClassValue(elements, metaFor, m -> m.value())).asElement();
    }
  }

  protected void processMeta(RoundEnvironment env, T t, Element element) throws AssertException {
  }

  protected abstract void process(RoundEnvironment env, T t, AnnotationMirror mid, Element element)
      throws AssertException;

  protected String metaPath(Class<?> metaClass) {
    return "META-INF/META-ANNOTATION/" + metaClass.getCanonicalName();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>(annotatedAnnotationNames);
    set.add(metaClass.getCanonicalName());
    return set;
  }
}
