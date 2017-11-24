package xdean.annotation.processor.toolkit;

import static xdean.annotation.processor.toolkit.ElementUtil.getAnnotationMirror;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import xdean.annotation.processor.toolkit.annotation.SupportedAnnotation;
import xdean.annotation.processor.toolkit.annotation.SupportedAnnotations;

public abstract class XAbstractProcessor extends AbstractProcessor {

  protected Types types;
  protected Elements elements;
  protected Messager messager;
  protected boolean isDebug;
  private Log error, warning, debug;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
    types = processingEnv.getTypeUtils();
    elements = processingEnv.getElementUtils();
    isDebug = processingEnv.getOptions().containsKey("debug");
    error = new Log(Kind.ERROR, true);
    warning = new Log(Kind.WARNING, true);
    debug = new Log(Kind.NOTE, isDebug);
  }

  @Override
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    handleAssert(() -> processActual(annotations, roundEnv));
    return false;
  }

  public abstract boolean processActual(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
      throws AssertException;

  protected void handleAssert(Runnable r) {
    try {
      r.run();
    } catch (AssertException e) {
      if (e.getMessage() != null && !e.getMessage().isEmpty()) {
        error().log(e.getMessage());
      }
    }
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>();
    // Analyze @SupportedAnnotationTypes
    SupportedAnnotationTypes sat = this.getClass().getAnnotation(SupportedAnnotationTypes.class);
    if (sat != null) {
      Arrays.stream(sat.value()).forEach(set::add);
    }
    // Analyze @SupportedAnnotation(s)
    SupportedAnnotations sas = this.getClass().getAnnotation(SupportedAnnotations.class);
    if (sas == null) {
      SupportedAnnotation sa = this.getClass().getAnnotation(SupportedAnnotation.class);
      if (sa != null) {
        set.add(sa.value().getCanonicalName());
      }
    } else {
      Arrays.stream(sas.value()).map(sa -> sa.value().getCanonicalName()).forEach(set::add);
    }
    if (set.isEmpty() && isInitialized()) {
      debug().log("No SupportedAnnotationTypes annotation found on " + this.getClass().getName()
          + ", returning an empty set.");
    }
    return set;
  }

  /****************************** ASSERT **********************************/

  protected static class Assert<T> {
    boolean fail;
    T value;

    public Assert(boolean assertion) {
      this(null, assertion);
    }

    public Assert(T value, boolean assertion) {
      this.value = value;
      this.fail = !assertion;
    }

    public T message(String msg) {
      if (fail) {
        throw new AssertException(msg);
      }
      return value;
    }

    public T todo(Runnable task) {
      if (fail) {
        task.run();
        throw new AssertException();
      }
      return value;
    }
  }

  protected Assert<Void> assertThat(boolean b) {
    return new Assert<>(b);
  }

  @SuppressWarnings("unchecked")
  protected <T> Assert<T> assertType(Object o, Class<T> clz) {
    return new Assert<>((T) o, clz.isInstance(o));
  }

  /****************************** LOG *******************************/
  protected Log debug() {
    return debug;
  }

  protected Log warning() {
    return warning;
  }

  protected Log error() {
    return error;
  }

  protected class Log {
    Kind kind;
    boolean enable;

    public Log(Kind kind, boolean enable) {
      this.kind = kind;
      this.enable = enable;
    }

    public void log(String msg) {
      if (enable) {
        messager.printMessage(kind, msg);
      }
    }

    public void log(String msg, Element element) {
      if (enable) {
        messager.printMessage(kind, msg, element);
      }
    }

    public void log(String msg, Element element, Class<? extends Annotation> annotation) {
      if (enable) {
        Optional<AnnotationMirror> am = getAnnotationMirror(element, annotation);
        if (am.isPresent()) {
          log(msg, element, am.get());
        } else {
          messager.printMessage(kind, msg, element);
        }
      }
    }

    public void log(String msg, Element element, AnnotationMirror annotation) {
      if (enable) {
        messager.printMessage(kind, msg, element, annotation);
      }
    }

    public void log(String msg, Element element, Class<? extends Annotation> annotation, AnnotationValue av) {
      if (enable) {
        Optional<AnnotationMirror> am = getAnnotationMirror(element, annotation);
        if (am.isPresent()) {
          log(msg, element, am.get(), av);
        } else {
          messager.printMessage(kind, msg, element);
        }
      }
    }

    public void log(String msg, Element element, AnnotationMirror annotation, AnnotationValue av) {
      if (enable) {
        messager.printMessage(kind, msg, element, annotation, av);
      }
    }
  }
}
