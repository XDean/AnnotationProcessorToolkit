package xdean.annotation.processor.toolkit;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public interface ElementUtil {

  /**
   * For Class attribute, if we invoke directly, it will throw
   * {@link MirroredTypeException}. Use this method to get the Class value
   * safely.
   *
   * @param elements Elements for convert Class to TypeMirror
   * @param anno annotation object
   * @param func the invocation of get Class value
   * @return the value's {@link TypeMirror}
   */
  public static <T extends Annotation> TypeMirror getAnnotationClassValue(Elements elements, T anno,
      Function<T, Class<?>> func) {
    try {
      return elements.getTypeElement(func.apply(anno).getCanonicalName()).asType();
    } catch (MirroredTypeException e) {
      return e.getTypeMirror();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Annotation> List<TypeMirror> getAnnotationClassArray(
      Elements elements, T anno, Function<T, Class<?>[]> func) {
    try {
      return Arrays.stream(func.apply(anno))
          .<TypeMirror> map(c -> elements.getTypeElement(c.getCanonicalName()).asType())
          .collect(Collectors.toList());
    } catch (MirroredTypesException e) {
      return (List<TypeMirror>) e.getTypeMirrors();
    }
  }

  /**
   * Get {@link AnnotationMirror} from given element with given type
   *
   * @param element the element to find annotation
   * @param annotationClass the annotation class
   * @return the AnnotationMirror
   */
  public static Optional<AnnotationMirror> getAnnotationMirror(Element element,
      Class<? extends Annotation> annotationClass) {
    return getAnnotationMirror(element, annotationClass.getCanonicalName());
  }

  /**
   * Get {@link AnnotationMirror} from given element with given type
   *
   * @param element the element to find annotation
   * @param annotationType the annotation {@link TypeMirror}
   * @return the AnnotationMirror
   */
  public static Optional<AnnotationMirror> getAnnotationMirror(Element element, TypeMirror annotationType) {
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

  public static List<? extends AnnotationMirror> getInheritAnnotationMirrors(TypeElement element) {
    if (element.getKind() != ElementKind.CLASS || element.getSuperclass() instanceof NoType) {
      return element.getAnnotationMirrors();
    }
    TypeElement sup = (TypeElement) ((DeclaredType) element.getSuperclass()).asElement();
    List<AnnotationMirror> list = new ArrayList<>(element.getAnnotationMirrors());
    List<AnnotationMirror> collect = getInheritAnnotationMirrors(sup)
        .stream()
        .filter(a -> isInherit(a.getAnnotationType()))
        .filter(a -> !list.stream()
            .anyMatch(t -> Objects.equals(t.getAnnotationType(), a.getAnnotationType())))
        .collect(Collectors.toList());
    list.addAll(collect);
    return list;
  }

  public static boolean isInherit(DeclaredType annoType) {
    return getAnnotationMirror(annoType.asElement(), Inherited.class).isPresent();
  }

  public static Stream<TypeElement> getAllSubClasses(Types types, RoundEnvironment env, TypeMirror type) {
    return env.getRootElements()
        .stream()
        .flatMap(e -> getAllSubClasses(types, e, type));
  }

  public static Stream<TypeElement> getAllSubClasses(Types types, Element root, TypeMirror type) {
    List<ElementKind> list = Arrays.asList(ElementKind.CLASS, ElementKind.ENUM, ElementKind.INTERFACE);
    return Stream.concat(Stream.of(root)
        .filter(e -> list.contains(e.getKind()))
        .map(e -> (TypeElement) e)
        .filter(e -> types.isAssignable(types.erasure(e.asType()), type)),
        root.getEnclosedElements().stream().flatMap(e -> getAllSubClasses(types, e, type)));
  }
}
