package xdean.annotation.processor;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import xdean.annotation.processor.annotation.SupportedAnnotation;
import xdean.annotation.processor.annotation.SupportedAnnotations;

public abstract class XAbstractProcessor extends AbstractProcessor {

  protected Messager messager;
  protected boolean isDebug;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
    isDebug = processingEnv.getOptions().containsKey("debug");
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    SupportedAnnotations sas = this.getClass().getAnnotation(SupportedAnnotations.class);
    if (sas == null) {
      SupportedAnnotation sa = this.getClass().getAnnotation(SupportedAnnotation.class);
      if (sa == null) {
        if (isInitialized()) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
              "No SupportedAnnotationTypes annotation " + "found on " + this.getClass().getName()
                  + ", returning an empty set.");
        }
        return Collections.emptySet();
      } else {
        return Collections.singleton(sa.value().getName());
      }
    } else {
      return Arrays.stream(sas.value()).map(sa -> sa.value().getName()).collect(Collectors.toSet());
    }
  }

  protected void debug(String msg) {
    if (isDebug) {
      messager.printMessage(Kind.NOTE, msg);
    }
  }

  protected void warning(String msg) {
    messager.printMessage(Kind.WARNING, msg);
  }

  protected void error(String msg, Element element, Class<? extends Annotation> annotation) {
    Optional<AnnotationMirror> am = getAnnotationMirror(element, annotation);
    if (am.isPresent()) {
      messager.printMessage(Kind.ERROR, msg, element, am.get());
    } else {
      messager.printMessage(Kind.ERROR, msg, element);
    }
  }

  public static Optional<AnnotationMirror> getAnnotationMirror(
      Element element, Class<? extends Annotation> annotationClass) {
    String annotationClassName = annotationClass.getCanonicalName();
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      TypeElement annotationTypeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
      if (annotationTypeElement.getQualifiedName().contentEquals(annotationClassName)) {
        return Optional.of(annotationMirror);
      }
    }
    return Optional.empty();
  }
}
