package xdean.annotation.processor.toolkit.meta;

import org.junit.Before;
import org.junit.Test;

public class UseMeta {
  @MetaedAnno
  public void func1() {
  }

  @MetaAnno
  public void func2() {
  }

  @NestMetaAnno
  public void func3() {
  }

  @Test
  public void func4() {
  }

  @Before
  public void func5() {
  }

  // this is meta for, should not process
  @NestMetaForAnno
  public void func6() {
  }
}
