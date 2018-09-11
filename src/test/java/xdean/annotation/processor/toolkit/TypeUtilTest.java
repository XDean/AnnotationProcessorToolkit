package xdean.annotation.processor.toolkit;

import static org.junit.Assert.assertEquals;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import xdean.test.compile.Compile;
import xdean.test.compile.CompileTestCase;

public class TypeUtilTest extends CompileTestCase {
  @Compile(sources = "Empty.java")
  public void testName(RoundEnvironment env) throws Exception {
    NoType t = types.getNoType(TypeKind.VOID);
    TypeMirror erasure = TypeUtil.erasure(types, t);
    assertEquals(t, erasure);
  }
}
