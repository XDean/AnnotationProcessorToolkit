package xdean.annotation.processor.toolkit;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import xdean.annotation.processor.toolkit.annotation.SupportedAnnotation;

public class XAbstractProcessorTest {
  @Test
  public void test() throws Exception {
    assertTrue(new A().getSupportedAnnotationTypes().isEmpty());
    assertThat(new B().getSupportedAnnotationTypes(), hasItems(Test.class.getName()));
    assertThat(new C().getSupportedAnnotationTypes(), hasItems(Test.class.getName(), Before.class.getName()));
    assertThat(new D().getSupportedAnnotationTypes(),
        hasItems(Test.class.getName(), Before.class.getName(), After.class.getName()));
  }

  static class XAP extends XAbstractProcessor {
    @Override
    public boolean processActual(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
        throws AssertException {
      return false;
    }
  }

  static class A extends XAP {
  }

  @SupportedAnnotation(Test.class)
  static class B extends XAP {
  }

  @SupportedAnnotation(Test.class)
  @SupportedAnnotation(Before.class)
  static class C extends XAP {
  }

  @SupportedAnnotationTypes("org.junit.After")
  @SupportedAnnotation(Test.class)
  @SupportedAnnotation(Before.class)
  static class D extends XAP {
  }
}
