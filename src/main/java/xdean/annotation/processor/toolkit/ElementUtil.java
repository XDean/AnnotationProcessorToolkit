package xdean.annotation.processor.toolkit;

import java.lang.annotation.Annotation;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class ElementUtil {

  /**
   * Get {@link AnnotationMirror} from given element with given type
   *
   * @param element the element to find annotation
   * @param annotationClass the annotation class
   * @return the AnnotationMirror
   */
  public static Optional<AnnotationMirror> getAnnotationMirror(
      Element element, Class<? extends Annotation> annotationClass) {
    return getAnnotationMirror(element, annotationClass.getCanonicalName());
  }

  /**
   * Get {@link AnnotationMirror} from given element with given type
   *
   * @param element the element to find annotation
   * @param annotationType the annotation {@link TypeMirror}
   * @return the AnnotationMirror
   */
  public static Optional<AnnotationMirror> getAnnotationMirror(
      Element element, TypeMirror annotationType) {
    return getAnnotationMirror(element, annotationType.toString());
  }

  /**
   * Get {@link AnnotationMirror} from given element with given annotation name
   *
   * @param element the element to find annotation
   * @param annoClzCanonicalName the annotation name
   * @return the AnnotationMirror
   */
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
