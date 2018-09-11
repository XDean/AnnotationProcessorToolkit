package xdean.annotation.processor.toolkit.meta;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import com.google.testing.compile.Compilation;

import xdean.annotation.processor.toolkit.AssertException;
import xdean.annotation.processor.toolkit.annotation.SupportedMetaAnnotation;
import xdean.test.compile.CompileTestCase;
import xdean.test.compile.Compiled;

public class TestMetaProcessor extends CompileTestCase {

  private static final List<Object> LIST = new ArrayList<>();

  @Compiled(
      sources = {
          "MetaedAnno.java",
          "MetaForAnno.java",
          "UseMeta.java"
      },
      processors = MetaProcessor.class)
  public void test(Compilation compilation) {
    System.out.println(LIST);
    assertEquals(5, LIST.size());
  }

  @SupportedMetaAnnotation(MetaAnno.class)
  public static class MetaProcessor extends AbstractMetaProcessor<MetaAnno> {
    @Override
    protected void process(RoundEnvironment env, MetaAnno t, AnnotationMirror mid, Element element) throws AssertException {
      LIST.add(element);
    }
  }
}
