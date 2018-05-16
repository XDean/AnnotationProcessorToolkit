package xdean.annotation.processor.toolkit;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.junit.Test;

import io.reactivex.Observable;
import xdean.annotation.processor.toolkit.getClass.Anno;
import xdean.test.compile.Compile;
import xdean.test.compile.CompileTestCase;

public class ElementUtilTest extends CompileTestCase {

  @Test
  @Compile(sources = {
      "getSub/A.java",
      "getSub/B.java",
      "getSub/C.java"
  })
  public void testGetAllSubClass(RoundEnvironment env) throws Exception {
    Observable.fromIterable(() -> ElementUtil.getAllSubClasses(types, env,
        elements.getTypeElement(Runnable.class.getCanonicalName()).asType()).iterator())
        .map(te -> te.getSimpleName().toString())
        .test()
        .assertValueCount(2)
        .assertValues("B", "C");
    Observable.fromIterable(() -> ElementUtil.getAllSubClasses(types, env,
        elements.getTypeElement(Serializable.class.getCanonicalName()).asType()).iterator())
        .map(te -> te.getSimpleName().toString())
        .test()
        .assertValueCount(2)
        .assertValues("A", "C");
  }

  @Test
  @Compile(sources = {
      "getClass/HaveCompiled.java",
      "getClass/NotCompiled.java"
  })
  public void testGetClassValue(RoundEnvironment env) throws Exception {
    TypeElement have = elements.getTypeElement("xdean.annotation.processor.toolkit.getClass.HaveCompiled");
    Observable
        .fromCallable(() -> ElementUtil.getAnnotationClassValue(elements, have.getAnnotation(Anno.class), a -> a.classValue()))
        .map(tm -> tm.toString())
        .test()
        .assertValues(CommonUtil.class.getName());
    Observable.fromCallable(
        () -> ElementUtil.getAnnotationClassArray(elements, have.getAnnotation(Anno.class), a -> a.classArray()))
        .flatMap(Observable::fromIterable)
        .map(tm -> tm.toString())
        .test()
        .assertValues(ElementUtil.class.getName(), AssertException.class.getName());
    TypeElement not = elements.getTypeElement("xdean.annotation.processor.toolkit.getClass.NotCompiled");
    Observable
        .fromCallable(() -> ElementUtil.getAnnotationClassValue(elements, not.getAnnotation(Anno.class), a -> a.classValue()))
        .map(tm -> tm.toString())
        .test()
        .assertValues("xdean.annotation.processor.toolkit.getClass.NotCompiled");
    Observable.fromCallable(
        () -> ElementUtil.getAnnotationClassArray(elements, not.getAnnotation(Anno.class), a -> a.classArray()))
        .flatMap(Observable::fromIterable)
        .map(tm -> tm.toString())
        .test()
        .assertValues(
            "xdean.annotation.processor.toolkit.getClass.A",
            "xdean.annotation.processor.toolkit.getClass.B");
  }

  @Test
  @Compile(sources = {
      "inherit/A.java",
      "inherit/B.java",
      "inherit/C.java",
      "inherit/D.java",
      "inherit/Inherit.java",
      "inherit/NotInherit.java",
  })
  public void testInherit(RoundEnvironment env) throws Exception {
    TypeElement c = elements.getTypeElement("xdean.annotation.processor.toolkit.inherit.C");
    List<? extends AnnotationMirror> cAnno = ElementUtil.getInheritAnnotationMirrors(c);
    assertEquals(1, cAnno.size());
    assertEquals("2", cAnno.get(0).getElementValues().values().stream().findFirst().get().toString());

    TypeElement d = elements.getTypeElement("xdean.annotation.processor.toolkit.inherit.D");
    List<? extends AnnotationMirror> dAnno = ElementUtil.getInheritAnnotationMirrors(d);
    assertEquals(2, dAnno.size());
    Object o1 = dAnno.get(0).getElementValues().values().stream().findFirst().get().getValue();
    Object o2 = dAnno.get(1).getElementValues().values().stream().findFirst().get().getValue();
    assertEquals(4, o1);
    assertEquals(2, o2);
  }
}
