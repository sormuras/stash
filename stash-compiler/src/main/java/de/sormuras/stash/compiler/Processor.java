package de.sormuras.stash.compiler;

import static java.lang.String.format;

import de.sormuras.beethoven.Annotation;
import de.sormuras.beethoven.type.ClassType;
import de.sormuras.beethoven.type.Type;
import de.sormuras.beethoven.unit.*;
import de.sormuras.stash.Stash;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class Processor extends AbstractProcessor {

  private boolean verbose = Boolean.getBoolean("de.sormuras.stash.compiler.verbose");

  private void error(Element element, String format, Object... args) {
    Diagnostic.Kind kind = Diagnostic.Kind.ERROR;
    processingEnv.getMessager().printMessage(kind, format(format, args), element);
  }

  private void print(String format, Object... args) {
    if (!verbose) {
      return;
    }
    Diagnostic.Kind kind = Diagnostic.Kind.NOTE;
    processingEnv.getMessager().printMessage(kind, format(format, args));
  }

  private String packageOf(Element element) {
    return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
  }

  private TypeElement element(Class<?> type) {
    return processingEnv.getElementUtils().getTypeElement(type.getCanonicalName());
  }

  private boolean isSameType(
      AnnotationMirror mirror, Class<? extends java.lang.annotation.Annotation> annotationClass) {
    TypeMirror overrideMirror = element(annotationClass).asType();
    return processingEnv.getTypeUtils().isSameType(mirror.getAnnotationType(), overrideMirror);
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
    processAllStashAnnotatedElements(round.getElementsAnnotatedWith(Stash.class));
    return true;
  }

  private void processAllStashAnnotatedElements(Set<? extends Element> stashAnnotatedElements) {
    for (Element stashAnnotated : stashAnnotatedElements) {
      ElementKind kind = stashAnnotated.getKind();
      if (!kind.isInterface() || kind == ElementKind.ANNOTATION_TYPE) {
        error(
            stashAnnotated,
            "@Stash expects an interface as target, not %s %s",
            kind,
            stashAnnotated);
      }
      processStashAnnotatedElement((TypeElement) stashAnnotated);
    }
  }

  private void processStashAnnotatedElement(TypeElement stashAnnotated) {
    Stash stash = stashAnnotated.getAnnotation(Stash.class);
    print("Interface %s is annotated with %s", stashAnnotated, stash);

    String packageName = packageOf(stashAnnotated);
    String simpleName = stashAnnotated.getSimpleName().toString();
    CompilationUnit unit = CompilationUnit.of(packageName);
    InterfaceDeclaration interfaceDeclaration = unit.declareInterface(simpleName);
    interfaceDeclaration.addAnnotation(Annotation.annotation(stash));
    processStashedInterface(stashAnnotated, interfaceDeclaration);
    print("Interface %s was declared as:%s", stashAnnotated, unit.list(" "));

    // generate...
    Generator generator = new Generator(stash, interfaceDeclaration);
    List<CompilationUnit> generatedUnits = generator.generate();
    for (CompilationUnit generated : generatedUnits) {
      print("Generated %s", generated.toURI());
      TypeDeclaration principal =
          generated.getEponymousDeclaration().orElseThrow(IllegalStateException::new);
      try {
        String sourceName = principal.toType().getName().canonical();
        JavaFileObject file = processingEnv.getFiler().createSourceFile(sourceName);
        try (PrintStream stream = new PrintStream(file.openOutputStream(), false, "UTF-8")) {
          stream.print(generated.list());
        }
      } catch (Exception e) {
        error(stashAnnotated, e.toString());
      }
    }
  }

  private void processStashedInterface(TypeElement stashed, InterfaceDeclaration declaration) {
    stashed.getInterfaces().forEach(i -> declaration.addInterface(ClassType.type(i)));
    // TODO stashed.getTypeParameters().forEach(p -> declaration.addTypeParameter(TypeParameter.of(p));
    List<ExecutableElement> methods =
        ElementFilter.methodsIn(processingEnv.getElementUtils().getAllMembers(stashed));
    methods.removeIf(m -> /* m.isDefault() || */ m.getModifiers().contains(Modifier.STATIC));
    methods.removeIf(m -> m.getEnclosingElement().equals(element(Object.class)));
    // methods.removeIf(m -> m.getEnclosingElement().equals(element(AutoCloseable.class)));
    // methods.removeIf(m -> m.getEnclosingElement().equals(element(Stash.class)));

    //		List<MethodDeclaration> methodInfos = new ArrayList<>(methods.size());
    for (ExecutableElement method : methods) {
      DeclaredType decodecl = (DeclaredType) stashed.asType();
      ExecutableType et;
      try {
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=481555
        et = (ExecutableType) processingEnv.getTypeUtils().asMemberOf(decodecl, method);
      } catch (IllegalArgumentException e) {
        // warn(method, e.toString());
        et = (ExecutableType) method.asType();
      }
      print("  %s -> %s", et, method);
      declaration.declareMethod(processStashedMethod(stashed, method, et));
    }
  }

  private MethodDeclaration processStashedMethod(
      TypeElement stashed, ExecutableElement method, ExecutableType executableType) {
    //
    MethodDeclaration declaration = new MethodDeclaration();
    declaration.setName(method.getSimpleName().toString());
    declaration.addModifiers(method.getModifiers());
    method
        .getAnnotationMirrors()
        .stream()
        .filter(a -> !isSameType(a, Override.class))
        .forEach(a -> declaration.addAnnotation(Type.Mirrors.annotation(a)));
    // TODO method.getTypeParameters().forEach(e -> builder.addTypeVariable(TypeVariableName.get((TypeVariable) e.asType())));
    method.getThrownTypes().forEach(t -> declaration.addThrows((ClassType) ClassType.type(t)));
    declaration.setReturnType(Type.type(method.getReturnType()));
    // parameters
    List<? extends VariableElement> parameters = method.getParameters();
    List<? extends TypeMirror> paramtypes = executableType.getParameterTypes();
    for (int index = 0; index < parameters.size(); index++) {
      TypeMirror type = paramtypes.get(index);
      Element element = processingEnv.getTypeUtils().asElement(type);
      VariableElement parameter = parameters.get(index);
      String name = parameter.getSimpleName().toString();
      MethodParameter methodParameter = declaration.declareParameter(Type.type(type), name);
      methodParameter.setFinal(parameter.getModifiers().contains(Modifier.FINAL));
      parameter
          .getAnnotationMirrors()
          .forEach(a -> methodParameter.addAnnotation(Type.Mirrors.annotation(a)));
      setParameterIsEnum(methodParameter, element != null && element.getKind() == ElementKind.ENUM);
      // TODO setParameterIsStashable(methodParameter, processingEnv.getTypeUtils().isAssignable(type, element(Stashable.class).asType()));
      // TODO setParameterIsTime(methodParameter, parameter.getAnnotation(Stash.Time.class) != null);
    }
    // calculate flags and other properties
    setMethodIsBase(
        declaration,
        method.getEnclosingElement().equals(element(AutoCloseable.class))
            || method.getEnclosingElement().equals(element(Stash.class)));
    setMethodIsChainable(
        declaration,
        processingEnv.getTypeUtils().isAssignable(stashed.asType(), method.getReturnType()));
    // TODO Stash.Volatile volanno = method.getAnnotation(Stash.Volatile.class);
    // TODO info.isVolatile = volanno != null;
    // TODO info.isDirect = volanno != null && volanno.direct();
    return declaration;
  }

  static void setMethodIsBase(MethodDeclaration method, boolean isBase) {
    method.getTags().put(Tag.METHOD_IS_BASE, isBase);
  }

  static void setMethodIsChainable(MethodDeclaration method, boolean isChainable) {
    method.getTags().put(Tag.METHOD_IS_CHAINABLE, isChainable);
  }

  static void setParameterIsEnum(MethodParameter parameter, boolean isEnum) {
    parameter.getTags().put(Tag.PARAMETER_IS_ENUM, isEnum);
  }

  static boolean isParameterEnum(MethodParameter parameter) {
    return (boolean) parameter.getTag(Tag.PARAMETER_IS_ENUM).orElseThrow(Error::new);
  }
}
