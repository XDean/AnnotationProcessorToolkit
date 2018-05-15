package xdean.annotation.processor.toolkit;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public interface TypeUtil {
  /**
   * Safe erasure type because ECJ bug.<br>
   * 
   * java.lang.ClassCastException: org.eclipse.jdt.internal.compiler.apt.model.NoTypeImpl cannot be
   * cast to org.eclipse.jdt.internal.compiler.apt.model.TypeMirrorImpl
   */
  static TypeMirror erasure(Types types, TypeMirror tm) {
    return tm.getKind() == TypeKind.VOID ? tm : types.erasure(tm);
  }
}
