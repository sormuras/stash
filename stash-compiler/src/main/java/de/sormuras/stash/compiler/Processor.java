/*
 * Copyright (C) 2017 Christian Stein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.sormuras.stash.compiler;

import static de.sormuras.stash.compiler.Tag.setMethodIsBase;
import static de.sormuras.stash.compiler.Tag.setMethodIsChainable;
import static de.sormuras.stash.compiler.Tag.setMethodIsDirect;
import static de.sormuras.stash.compiler.Tag.setMethodIsVolatile;
import static de.sormuras.stash.compiler.Tag.setParameterIsEnum;
import static de.sormuras.stash.compiler.Tag.setParameterIsStashable;
import static de.sormuras.stash.compiler.Tag.setParameterIsTime;
import static java.lang.String.format;

import de.sormuras.beethoven.Annotation;
import de.sormuras.beethoven.type.ClassType;
import de.sormuras.beethoven.type.Type;
import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.beethoven.unit.InterfaceDeclaration;
import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.MethodParameter;
import de.sormuras.beethoven.unit.TypeDeclaration;
import de.sormuras.stash.Stash;
import de.sormuras.stash.Stashable;
import de.sormuras.stash.Time;
import de.sormuras.stash.Volatile;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class Processor extends AbstractProcessor {

  private int roundCounter = 0;
  private boolean verbose = Boolean.getBoolean("de.sormuras.stash.compiler.verbose");

  private void error(Element element, String format, Object... args) {
    Diagnostic.Kind kind = Diagnostic.Kind.ERROR;
    processingEnv.getMessager().printMessage(kind, format(format, args), element);
  }

  private void note(String format, Object... args) {
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

  private boolean isAssignable(TypeMirror t1, Class<?> t2) {
    return isAssignable(t1, element(t2).asType());
  }

  private boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    return processingEnv.getTypeUtils().isAssignable(t1, t2);
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
    note("Round #%d -> %s", roundCounter, round);
    if (round.processingOver()) {
      return true;
    }
    if (roundCounter == 0) {
      copyStashable();
    }
    processAllStashAnnotatedElements(round.getElementsAnnotatedWith(Stash.class));
    roundCounter++;
    return true;
  }

  private void copyStashable() {
    try {
      String sourceName = Stashable.class.getCanonicalName();
      String sourceText = loadJavaSource(sourceName.replace('.', '/') + ".java");
      JavaFileObject file = processingEnv.getFiler().createSourceFile(sourceName);
      try (PrintStream stream = new PrintStream(file.openOutputStream(), false, "UTF-8")) {
        stream.print(sourceText);
      }
    } catch (Exception exception) {
      error(null, "Stashable.java creation failed: %s", exception.toString());
    }
  }

  private String loadJavaSource(String name) throws Exception {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
    if (inputStream == null) {
      inputStream = Files.newInputStream(Paths.get("../stash-api/src/main/java", name));
    }
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result.toString("UTF-8");
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
    note("Interface %s is annotated with %s", stashAnnotated, stash);

    String packageName = packageOf(stashAnnotated);
    String simpleName = stashAnnotated.getSimpleName().toString();
    CompilationUnit unit = CompilationUnit.of(packageName);
    InterfaceDeclaration interfaceDeclaration = unit.declareInterface(simpleName);
    interfaceDeclaration.addAnnotation(Annotation.annotation(stash));
    processStashedInterface(stashAnnotated, interfaceDeclaration);
    note("Interface %s was declared as:%s", stashAnnotated, unit.list(" "));

    // generate...
    Generator generator = new Generator(stash, interfaceDeclaration);
    List<CompilationUnit> generatedUnits = generator.generate();
    for (CompilationUnit generated : generatedUnits) {
      note("Generated %s", generated.toURI());
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

    for (ExecutableElement method : methods) {
      DeclaredType declaredType = (DeclaredType) stashed.asType();
      ExecutableType et;
      try {
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=481555
        et = (ExecutableType) processingEnv.getTypeUtils().asMemberOf(declaredType, method);
      } catch (IllegalArgumentException e) {
        et = (ExecutableType) method.asType();
      }
      declaration.declareMethod(processStashedMethod(stashed, method, et));
    }
  }

  private MethodDeclaration processStashedMethod(
      TypeElement stashed, ExecutableElement method, ExecutableType executableType) {
    // method signature
    MethodDeclaration declaration = new MethodDeclaration();
    declaration.setName(method.getSimpleName().toString());
    declaration.addModifiers(method.getModifiers());
    declaration.addAnnotations(Type.Mirrors.annotations(method));
    // TODO method.getTypeParameters().forEach(e -> builder.addTypeVariable(TypeVariableName.get((TypeVariable) e.asType())));
    method.getThrownTypes().forEach(t -> declaration.addThrows((ClassType) ClassType.type(t)));
    declaration.setReturnType(Type.type(method.getReturnType()));
    // parameters
    List<? extends VariableElement> parameters = method.getParameters();
    List<? extends TypeMirror> parameterTypes = executableType.getParameterTypes();
    for (int index = 0; index < parameters.size(); index++) {
      TypeMirror type = parameterTypes.get(index);
      Element element = processingEnv.getTypeUtils().asElement(type);
      VariableElement parameter = parameters.get(index);
      String name = parameter.getSimpleName().toString();
      MethodParameter methodParameter = declaration.declareParameter(Type.type(type), name);
      methodParameter.setFinal(parameter.getModifiers().contains(Modifier.FINAL));
      methodParameter.addAnnotations(Type.Mirrors.annotations(parameter));
      setParameterIsEnum(methodParameter, element != null && element.getKind() == ElementKind.ENUM);
      setParameterIsStashable(methodParameter, isAssignable(type, Stashable.class));
      setParameterIsTime(methodParameter, parameter.getAnnotation(Time.class) != null);
    }
    // calculate flags and other properties
    Element enclosingElement = method.getEnclosingElement();
    Volatile volatileAnnotation = method.getAnnotation(Volatile.class);
    setMethodIsBase(declaration, enclosingElement.equals(element(AutoCloseable.class)));
    setMethodIsChainable(declaration, isAssignable(stashed.asType(), method.getReturnType()));
    setMethodIsVolatile(declaration, volatileAnnotation != null);
    setMethodIsDirect(declaration, volatileAnnotation != null && volatileAnnotation.direct());
    return declaration;
  }
}
