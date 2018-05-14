package xdean.annotation.processor.toolkit.meta;

import static javax.lang.model.SourceVersion.RELEASE_8;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;

import xdean.annotation.processor.toolkit.AssertException;
import xdean.annotation.processor.toolkit.NestCompileFile;
import xdean.annotation.processor.toolkit.XAbstractProcessor;

@SupportedSourceVersion(RELEASE_8)
@SupportedAnnotationTypes("*")
@AutoService(Processor.class)
public class MetaAnnotationProcessor extends XAbstractProcessor {

  public static final String META_PATH = "META-INF/xdean/apt/";
  NestCompileFile meta = new NestCompileFile("META-INF/xdean/apt/" + Meta.class.getName());

  @Override
  public boolean processActual(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws AssertException {
    if (roundEnv.processingOver()) {
      assertDo(() -> updateMeta(roundEnv)).todo(() -> error().log("Fail to update META file."));
      updateAnnos(roundEnv);
      return true;
    }
    return true;
  }

  private void processMeta(RoundEnvironment roundEnv) {

  }

  private void updateMeta(RoundEnvironment roundEnv) throws IOException {
    PrintStream writer = meta.getPrintStream(filer);
    roundEnv.getElementsAnnotatedWith(Meta.class).forEach(e -> writer.println(e.asType().toString()));
    writer.close();
  }

  private void updateAnnos(RoundEnvironment roundEnv) {

  }
}
