package xdean.annotation.processor.toolkit;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import xdean.codecov.CodecovIgnore;
import xdean.jex.log.LogFactory;

@CodecovIgnore
public interface CommonUtil {
  static String getStackTraceString(Throwable tr) {
    if (tr == null) {
      return "";
    }
    Throwable t = tr;
    while (t.getCause() != null) {
      t = t.getCause();
    }
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();
    return sw.toString();
  }

  static void ensureFileSystem(URI uri) throws IOException {
    try {
      FileSystems.getFileSystem(uri);
    } catch (FileSystemNotFoundException e) {
      Map<String, String> env = new HashMap<>();
      env.put("create", "true");
      FileSystems.newFileSystem(uri, env);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Throwable, R> R throwAsUncheck(Throwable t) throws T {
    throw (T) t;
  }

  public static void uncheck(ActionE0<?> task) {
    try {
      task.call();
    } catch (Exception t) {
      throwAsUncheck(t);
    }
  }

  public static <T> T uncheck(FuncE0<T, ?> task) {
    try {
      return task.call();
    } catch (Exception t) {
      return throwAsUncheck(t);
    }
  }

  public static <T> void uncatch(ActionE0<?> task) {
    try {
      task.call();
    } catch (Exception t) {
      LogFactory.from(CommonUtil.class).debug("uncatch", t);
    }
  }

  public static <T> T uncatch(FuncE0<T, ?> task) {
    try {
      return task.call();
    } catch (Exception t) {
      return null;
    }
  }

  @FunctionalInterface
  public interface ActionE0<E extends Exception> {
    void call() throws E;
  }

  @FunctionalInterface
  public interface FuncE0<R, E extends Exception> extends Callable<R> {
    @Override
    R call() throws E;
  }

}
