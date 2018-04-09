package xdean.annotation.processor.toolkit;

import java.io.Serializable;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import org.junit.Test;

import io.reactivex.Observable;
import xdean.annotation.processor.toolkit.getClass.Anno;
import xdean.annotation.processor.toolkit.test.Compile;
import xdean.annotation.processor.toolkit.test.CompileTest;

public class ElementUtilTest extends CompileTest {

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
}
