package xdean.annotation.processor.toolkit.meta;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import xdean.annotation.processor.toolkit.AssertException;
import xdean.annotation.processor.toolkit.NestCompileFile;
import xdean.annotation.processor.toolkit.XAbstractProcessor;
import xdean.annotation.processor.toolkit.annotation.SupportedMetaAnnotation;

public abstract class AbstractMetaProcessor<T extends Annotation> extends XAbstractProcessor {
  public static final String META_PATH = "META-INF/xdean/apt/";

  protected final Class<T> metaClass;
  protected final NestCompileFile metaFile;
  protected final List<String> annotatedClasses;

  @SuppressWarnings("unchecked")
  public AbstractMetaProcessor() {
    SupportedMetaAnnotation meta = this.getClass().getAnnotation(SupportedMetaAnnotation.class);
    if (meta == null) {
      throw new Error("AbstractMetaProcessor must use with @SupportedMetaAnnotation.");
    } else if (meta.value().isAnnotationPresent(Meta.class)) {
      throw new Error("AbstractMetaProcessor only can process Annotation with @Meta.");
    }
    metaClass = (Class<T>) meta.value();
    metaFile = new NestCompileFile(META_PATH + meta.value().getCanonicalName());
    try {
      annotatedClasses = metaFile.readLines().collect(Collectors.toList());
    } catch (IOException e) {
      throw new Error("Fail to read meta file.", e);
    }
  }

  @Override
  public boolean processActual(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws AssertException {
    if (roundEnv.processingOver()) {
      return false;
    }
    roundEnv.getElementsAnnotatedWith(metaClass);
    return false;
  }

  protected abstract void process(RoundEnvironment env);

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>(annotatedClasses);
    set.add(metaClass.getCanonicalName());
    return set;
  }
}
