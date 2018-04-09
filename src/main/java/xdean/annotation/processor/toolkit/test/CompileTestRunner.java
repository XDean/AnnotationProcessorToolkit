package xdean.annotation.processor.toolkit.test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.JavaFileObject;

import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.core.annotation.AnnotationUtils;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import xdean.annotation.processor.toolkit.CommonUtil;

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
    Class<?> paramType = method.getMethod().getParameterTypes()[0];
    CompileTest ct = (CompileTest) test;
    if (paramType == RoundEnvironment.class) {
      ct.setMethod(method);
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          Compile compile = AnnotationUtils.getAnnotation(method.getMethod(), Compile.class);
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
    } else if (paramType == Compilation.class) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          Compiled compiled = AnnotationUtils.getAnnotation(method.getMethod(), Compiled.class);
          Class<?> clz = getTestClass().getJavaClass();
          Compilation compilation = Compiler.javac()
              .withProcessors(Arrays.stream(compiled.processors())
                  .map(c -> CommonUtil.uncheck(() -> c.newInstance()))
                  .toArray(Processor[]::new))
              .withOptions(Arrays.stream(compiled.options()).toArray(Object[]::new))
              .compile(Arrays.stream(compiled.sources())
                  .map(s -> clz.getResource(s))
                  .map(u -> JavaFileObjects.forResource(u))
                  .toArray(JavaFileObject[]::new));
          method.invokeExplosively(ct, compilation);
        }
      };
    } else {
      return super.methodInvoker(method, test);
    }
  }

  @Override
  protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic,
      List<Throwable> errors) {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
    for (FrameworkMethod method : methods) {
      method.validatePublicVoid(isStatic, errors);
      int pc = method.getMethod().getParameterCount();
      if (pc != 1) {
        errors.add(new Exception("Method " + method.getName() + "() should have 0 or 1 parameter"));
      } else if (pc == 1) {
        Class<?> paramType = method.getMethod().getParameterTypes()[0];
        if (paramType == RoundEnvironment.class) {
          if (!method.getMethod().isAnnotationPresent(Compile.class)) {
            errors.add(new Exception("Method " + method.getName() + "() should annotated @Compile."));
          }
        } else if (paramType == Compilation.class) {
          if (!method.getMethod().isAnnotationPresent(Compiled.class)) {
            errors.add(new Exception("Method " + method.getName() + "() should annotated @Compiled."));
          }
        } else {
          errors.add(new Exception("Method " + method.getName() + "()'s parameter only can be RoundEnvironment or Compilation."));
        }
      }
    }
  }
}
