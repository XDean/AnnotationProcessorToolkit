package xdean.annotation.processor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.Diagnostic;

import xdean.annotation.processor.annotation.SupportedAnnotations;

public abstract class XAbstractProcessor extends AbstractProcessor {
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    SupportedAnnotations sas = this.getClass().getAnnotation(SupportedAnnotations.class);
    if (sas == null) {
      if (isInitialized()) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
            "No SupportedAnnotationTypes annotation " + "found on " + this.getClass().getName()
                + ", returning an empty set.");
      }
      return Collections.emptySet();
    } else {
      return Arrays.stream(sas.value()).map(sa -> sa.value().getName()).collect(Collectors.toSet());
    }
  }
}
