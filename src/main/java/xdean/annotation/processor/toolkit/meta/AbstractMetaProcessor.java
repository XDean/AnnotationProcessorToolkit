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
import java.util.stream.Stream;

import javax.annotation.CheckForNull;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import xdean.annotation.processor.toolkit.AssertException;
import xdean.annotation.processor.toolkit.ElementUtil;
import xdean.annotation.processor.toolkit.NestCompileFile;
import xdean.annotation.processor.toolkit.XAbstractProcessor;
import xdean.annotation.processor.toolkit.annotation.Meta;
import xdean.annotation.processor.toolkit.annotation.SupportedMetaAnnotation;

public abstract class AbstractMetaProcessor<T extends Annotation> extends XAbstractProcessor {
  public static final String META_PATH = "META-INF/xdean/apt/";

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
    metaFile = new NestCompileFile(META_PATH + meta.value().getCanonicalName());
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
    List<TypeElement> annotatedAnnotations = map.getOrDefault(true, Collections.emptyList()).stream().map(e -> (TypeElement) e)
        .collect(Collectors.toList());
    List<Element> annotatedElements = map.getOrDefault(false, Collections.emptyList());
    annotatedElements.forEach(e -> handleAssert(() -> process(roundEnv, e.getAnnotation(metaClass), null, e)));
    Stream.concat(
        annotatedAnnotations.stream(),
        annotatedAnnotationNames.stream().map(s -> elements.getTypeElement(s)))
        .forEach(te -> {
          T meta = te.getAnnotation(metaClass);
          roundEnv.getElementsAnnotatedWith(te).forEach(
              e -> handleAssert(() -> process(roundEnv, meta, ElementUtil.getAnnotationMirror(e, te.asType()).get(), e)));
        });
    writeMetaClasses(annotatedAnnotations);
    return false;
  }

  public void writeMetaClasses(List<TypeElement> annotatedAnnotations) throws Error {
    try (PrintStream printer = metaFile.getPrintStream(filer)) {
      annotatedAnnotations.forEach(e -> printer.println(e.asType().toString()));
    } catch (IOException e) {
      throw new Error("Error to write meta file.", e);
    }
  }

  protected abstract void process(RoundEnvironment env, T t, @CheckForNull AnnotationMirror mid, Element element);

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>(annotatedAnnotationNames);
    set.add(metaClass.getCanonicalName());
    return set;
  }
}
