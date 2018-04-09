package xdean.annotation.processor.toolkit.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;

import xdean.annotation.processor.toolkit.XAbstractProcessor;

@Ignore
@RunWith(CompileTestRunner.class)
public class CompileTest extends XAbstractProcessor {
  private FrameworkMethod method;
  private Optional<Compile> anno;
  private Throwable error;

  public void setMethod(FrameworkMethod method) {
    this.method = method;
    this.anno = Optional.ofNullable(method.getAnnotation(Compile.class));
  }

  @Override
  public boolean processActual(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }
    try {
      method.invokeExplosively(this, roundEnv);
    } catch (Throwable e) {
      error = e;
    }
    return false;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return anno.map(c -> c.version()).orElse(SourceVersion.RELEASE_8);
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return anno.map(co -> Arrays.stream(co.annotations())
        .map(c -> c.getName())
        .collect(Collectors.toSet()))
        .filter(s -> !s.isEmpty())
        .orElse(Collections.singleton("*"));
  }

  public Throwable getError() {
    return error;
  }
}