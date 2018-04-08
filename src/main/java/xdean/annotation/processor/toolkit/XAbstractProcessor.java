package xdean.annotation.processor.toolkit;

import static xdean.annotation.processor.toolkit.ElementUtil.getAnnotationMirror;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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

/**
 * An more powerful abstract annotation processor designed to be a convenient superclass for most concrete annotation
 * processors.
 * <p>
 * Differences with {@link AbstractProcessor}:
 * <ul>
 * <li>Use {@link SupportedAnnotation} to define supported annotation by Class instead of String</li>
 * <li>Assert methods for quick exit rather than check and return</li>
 * <li>Convenient access to common utilities and log</li>
 * </ul>
 *
 * @author XDean
 */
public abstract class XAbstractProcessor extends AbstractProcessor implements CommonUtil {

  protected Types types;
  protected Elements elements;
  protected Messager messager;
  protected Filer filer;
  protected boolean isDebug;
  private Log error, warning, debug, noLog = new Log(Kind.OTHER, false);

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
    types = processingEnv.getTypeUtils();
    elements = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    isDebug = processingEnv.getOptions().containsKey("debug");
    error = new Log(Kind.ERROR, true);
    warning = new Log(Kind.WARNING, true);
    debug = new Log(Kind.NOTE, isDebug);
  }

  /**
   * Use {@link #processActual(Set, RoundEnvironment)}
   */
  @Override
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    handleAssert(() -> processActual(annotations, roundEnv));
    return false;
  }

  /**
   * Processes a set of annotation types on type elements originating from the prior round and returns whether or not
   * these annotation types are claimed by this processor. If {@code
   * true} is returned, the annotation types are claimed and subsequent processors will not be asked to process them; if
   * {@code false} is returned, the annotation types are unclaimed and subsequent processors may be asked to process
   * them. A processor may always return the same boolean value or may vary the result based on chosen criteria.
   *
   * <p>
   * The input set will be empty if the processor supports {@code
   * "*"} and the root elements have no annotations. A {@code
   * Processor} must gracefully handle an empty set of annotations.
   *
   * @param annotations the annotation types requested to be processed
   * @param roundEnv environment for information about the current and prior round
   * @return whether or not the set of annotation types are claimed by this processor
   * @throws AssertException throw the AssertException to quit the process directly
   * @see #assertThat(boolean)
   * @see #handleAssert(Runnable)
   */
  public abstract boolean processActual(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
      throws AssertException;

  /**
   * Override this method to do something for {@link AssertException}.<br>
   * The default behavior is log the message as error if present.
   *
   * @param task task who will throw AssertException
   */
  protected void handleAssert(Runnable task) {
    try {
      task.run();
    } catch (AssertException e) {
      if (e.getMessage() != null && !e.getMessage().isEmpty()) {
        error().log(e.getMessage());
      }
    }
  }

  /**
   * If the processor class is annotated with {@link SupportedAnnotation} or {@link SupportedAnnotationTypes}, return an
   * unmodifiable set with the same set of strings as the annotation. If the class is not so annotated, an empty set is
   * returned.
   */
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
    return Collections.unmodifiableSet(set);
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

    /**
     * If the assert fail, throw an {@link AssertException} with given message. Or return the success value.
     *
     * @param msg the fail message
     * @return if success, return the value
     */
    public T message(String msg) {
      if (fail) {
        throw new AssertException(msg);
      }
      return value;
    }

    /**
     * If the assert fail, do the task and then throw an {@link AssertException}. Or return the success value.
     *
     * @param task the task to do when fail
     * @return if success, return the value
     */
    public T todo(Runnable task) {
      if (fail) {
        task.run();
        throw new AssertException();
      }
      return value;
    }

    /**
     * If the assert fail, throw an {@link AssertException}. Or return the success value.
     *
     * @return if success, return the value
     */
    public T doNoThing() {
      return todo(() -> {
      });
    }
  }

  protected Assert<Void> assertThat(boolean b) {
    return new Assert<>(b);
  }

  @SuppressWarnings("unchecked")
  protected <T> Assert<T> assertType(Object o, Class<T> clz) {
    return new Assert<>((T) o, clz.isInstance(o));
  }

  protected <T> Assert<T> assertNonNull(T t) {
    return new Assert<>(t, t != null);
  }

  protected <T> Assert<T> assertDo(Callable<T> task){
    try {
      return new Assert<>(task.call(), true);
    } catch (Exception e) {
      return new Assert<>(false);
    }
  }

  /****************************** LOG *******************************/
  protected Log debug() {
    return isInitialized() ? debug : noLog;
  }

  protected Log warning() {
    return isInitialized() ? warning : noLog;
  }

  protected Log error() {
    return isInitialized() ? error : noLog;
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
