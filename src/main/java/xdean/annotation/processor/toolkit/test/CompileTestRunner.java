package xdean.annotation.processor.toolkit.test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.tools.JavaFileObject;

import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

public class CompileTestRunner extends BlockJUnit4ClassRunner {

  public CompileTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    super.collectInitializationErrors(errors);
    if (!CompileTest.class.isAssignableFrom(getTestClass().getJavaClass())) {
      errors.add(new Exception("CompileTestRunner must run with CompileTest"));
    }
  }

  @Override
  protected void validateTestMethods(List<Throwable> errors) {
    validatePublicVoidNoArgMethods(Test.class, false, errors);
  }

  @Override
  protected Statement methodInvoker(FrameworkMethod method, Object test) {
    CompileTest ct = (CompileTest) test;
    ct.setMethod(method);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Compile compile = method.getAnnotation(Compile.class);
        Class<?> clz = getTestClass().getJavaClass();
        Compiler.javac()
            .withProcessors(ct)
            .compile(Arrays.stream(compile.sources())
                .map(s -> clz.getResource(s))
                .map(u -> JavaFileObjects.forResource(u))
                .toArray(JavaFileObject[]::new));
        if (ct.getError() != null) {
          throw ct.getError();
        }
      }
    };
  }

  @Override
  protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic,
      List<Throwable> errors) {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
    for (FrameworkMethod eachTestMethod : methods) {
      eachTestMethod.validatePublicVoid(isStatic, errors);
      int pc = eachTestMethod.getMethod().getParameterCount();
      if (pc > 1) {
        errors.add(new Exception("Method " + eachTestMethod.getName() + "() should have 0 or 1 parameter"));
      } else if (pc == 1) {
        if (eachTestMethod.getMethod().getParameterTypes()[0] != RoundEnvironment.class) {
          errors.add(new Exception("Method " + eachTestMethod.getName() + "()'s parameter only can be RoundEnvironment."));
        }
      }
    }
  }
}
