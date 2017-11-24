package xdean.annotation.processor.toolkit;

import java.lang.annotation.Annotation;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class ElementUtil {

  public static Optional<AnnotationMirror> getAnnotationMirror(
      Element element, Class<? extends Annotation> annotationClass) {
    return getAnnotationMirror(element, annotationClass.getCanonicalName());
  }

  public static Optional<AnnotationMirror> getAnnotationMirror(
      Element element, TypeMirror annotationType) {
    return getAnnotationMirror(element, annotationType.toString());
  }

  public static Optional<AnnotationMirror> getAnnotationMirror(Element element, String annoClzCanonicalName) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      TypeElement annotationTypeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
      if (annotationTypeElement.getQualifiedName().contentEquals(annoClzCanonicalName)) {
        return Optional.of(annotationMirror);
      }
    }
    return Optional.empty();
  }
}
