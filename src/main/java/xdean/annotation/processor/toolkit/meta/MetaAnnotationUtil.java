package xdean.annotation.processor.toolkit.meta;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

public class MetaAnnotationUtil {
  /**
   * Get elements annotated with the annotation type. All class will be detected, not only current
   * compile-period classes.
   */
  public Set<Element> getElementsAnnotatedWith(RoundEnvironment env, Class<? extends Annotation> anno) {
    Set<Element> set = new HashSet<>();
    set.addAll(env.getElementsAnnotatedWith(anno));
    return set;
  }
}
