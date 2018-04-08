package xdean.annotation.processor.toolkit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class NestCompileFile {
  private final String name;

  public NestCompileFile(String name) {
    this.name = name;
  }

  public Writer getWriter(Filer filer) throws IOException {
    return new OutputStreamWriter(getOutputStream(filer), StandardCharsets.UTF_8);
  }

  public PrintStream getPrintStream(Filer filer) throws IOException {
    return new PrintStream(getOutputStream(filer), false, "UTF-8");
  }

  public OutputStream getOutputStream(Filer filer) throws IOException {
    FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", name);
    OutputStream output = resource.openOutputStream();
    return output;
  }

  public Stream<String> readLines() throws IOException {
    return Collections.list(getClass().getClassLoader().getResources(name))
        .stream()
        .flatMap(url -> CommonUtil.uncheck(() -> {
          URI uri = url.toURI();
          CommonUtil.ensureFileSystem(uri);
          return Files.lines(Paths.get(uri));
        }));
  }
}
