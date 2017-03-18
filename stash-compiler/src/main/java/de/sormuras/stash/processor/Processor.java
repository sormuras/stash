package de.sormuras.stash.processor;

import de.sormuras.beethoven.Annotation;
import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.beethoven.unit.InterfaceDeclaration;
import de.sormuras.beethoven.unit.TypeDeclaration;
import de.sormuras.stash.Stash;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class Processor extends AbstractProcessor {

  private void error(Element element, String format, Object... args) {
    processingEnv
        .getMessager()
        .printMessage(Diagnostic.Kind.ERROR, String.format(format, args), element);
  }

  private void print(String format, Object... args) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>();
    set.add(Stash.class.getCanonicalName());
    return set;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    if (round.processingOver()) {
      return true;
    }
    processAllStashes(round.getElementsAnnotatedWith(Stash.class));
    return true;
  }

  private void processAllStashes(Set<? extends Element> stashAnnotatedElements) {
    for (Element stashed : stashAnnotatedElements) {
      ElementKind kind = stashed.getKind();
      if (kind.isInterface()) {
        if (kind != ElementKind.ANNOTATION_TYPE) {
          processStashedElement((TypeElement) stashed);
          continue;
        }
      }
      error(stashed, "@Stash.Interface expects an interface as target, not %s %s", kind, stashed);
    }
  }

  private void processStashedElement(TypeElement stashed) {
    Stash stash = stashed.getAnnotation(Stash.class);
    print("Interface %s is annotated with %s", stashed, stash);

    String packageName =
        processingEnv.getElementUtils().getPackageOf(stashed).getQualifiedName().toString();
    String simpleName = stashed.getSimpleName().toString();
    CompilationUnit unit = CompilationUnit.of(packageName);
    InterfaceDeclaration interfaceDeclaration = unit.declareInterface(simpleName);
    interfaceDeclaration.addAnnotation(Annotation.annotation(stash));
    // TODO processStashedInterface(stashed, interfaceDeclaration);

    // generate...
    Generator generator = new Generator(interfaceDeclaration);
    List<CompilationUnit> generatedUnits = generator.generate();
    for (CompilationUnit generated : generatedUnits) {
      print("Generated %s", generated.toURI());
      TypeDeclaration principal =
          generated.getEponymousDeclaration().orElseThrow(IllegalStateException::new);
      try {
        String sourceName = principal.toType().getName().canonical();
        JavaFileObject file = processingEnv.getFiler().createSourceFile(sourceName);
        try (PrintStream stream = new PrintStream(file.openOutputStream(), true, "UTF-8")) {
          stream.print(generated.list());
        }
      } catch (Exception e) {
        error(stashed, e.toString());
      }
    }
  }
}
