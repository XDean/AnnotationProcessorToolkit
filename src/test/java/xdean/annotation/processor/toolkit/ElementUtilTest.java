package xdean.annotation.processor.toolkit;

import java.io.Serializable;

import javax.annotation.processing.RoundEnvironment;

import org.junit.Test;

import io.reactivex.Observable;

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
}
