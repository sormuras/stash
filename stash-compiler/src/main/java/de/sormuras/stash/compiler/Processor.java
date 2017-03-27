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
import static de.sormuras.stash.compiler.Tag.setParameterIsTime;
import static de.sormuras.stash.compiler.Tag.setTypeIsEnum;
import static de.sormuras.stash.compiler.Tag.setTypeIsStashable;
import static java.lang.String.format;

import de.sormuras.beethoven.Annotation;
import de.sormuras.beethoven.Name;
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
import de.sormuras.stash.compiler.stashlet.StaticStashlet;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.lang.model.util.Types;
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
    processAllStashAnnotatedElements(round.getElementsAnnotatedWith(Stash.class));
    roundCounter++;
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
    note("Interface %s is annotated with %s", stashAnnotated, stash);

    String packageName = packageOf(stashAnnotated);
    String simpleName = stashAnnotated.getSimpleName().toString();
    CompilationUnit unit = CompilationUnit.of(packageName);
    InterfaceDeclaration interfaceDeclaration = unit.declareInterface(simpleName);
    interfaceDeclaration.addAnnotation(Annotation.annotation(stash));
    Map<Type, Stashlet> stashlets = new HashMap<>();
    processStashedInterface(stashAnnotated, interfaceDeclaration, stashlets);
    note("Interface %s was declared as:%s", stashAnnotated, unit.list(" "));

    // generate...
    Generator generator = new Generator(stash, interfaceDeclaration);
    generator.getQuaestor().getCustoms().putAll(stashlets);
    List<CompilationUnit> generatedUnits = generator.generate();
    for (CompilationUnit generated : generatedUnits) {
      note("Generated %s", generated.toURI());
      TypeDeclaration principal = generated.getEponymousDeclaration().orElseThrow(Error::new);
      if (principal.isEmpty()) {
        note("Skipping empty %s", principal.getName());
        continue;
      }
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

  private void processStashedInterface(
      TypeElement stashed, InterfaceDeclaration declaration, Map<Type, Stashlet> stashlets) {
    List<ExecutableElement> staticMethods = new ArrayList<>();
    stashed.getInterfaces().forEach(i -> declaration.addInterface(ClassType.type(i)));
    // TODO stashed.getTypeParameters().forEach(p -> declaration.addTypeParameter(TypeParameter.of(p));
    List<ExecutableElement> methods =
        ElementFilter.methodsIn(processingEnv.getElementUtils().getAllMembers(stashed));

    for (ExecutableElement method : methods) {
      if (method.getEnclosingElement().equals(element(Object.class))) {
        continue;
      }
      if (method.getModifiers().contains(Modifier.STATIC)) {
        staticMethods.add(method);
        continue;
      }
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

    // turn static stash/spawn method pair into stashlet
    processAllStashletMethods(staticMethods, stashlets);
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
      Type parameterType = Type.type(type);
      MethodParameter methodParameter = declaration.declareParameter(parameterType, name);
      methodParameter.setFinal(parameter.getModifiers().contains(Modifier.FINAL));
      methodParameter.addAnnotations(Type.Mirrors.annotations(parameter));
      setTypeIsEnum(parameterType, element != null && element.getKind() == ElementKind.ENUM);
      setTypeIsStashable(parameterType, isAssignable(type, Stashable.class));
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

  private void processAllStashletMethods(
      Collection<ExecutableElement> staticMethods, Map<Type, Stashlet> stashlets) {
    Types types = processingEnv.getTypeUtils();
    Map<Type, ExecutableElement> spawns = new HashMap<>();
    Map<Type, ExecutableElement> stashs = new HashMap<>();
    for (Element accessor : staticMethods) {
      ExecutableElement method = (ExecutableElement) accessor;
      DeclaredType containing = (DeclaredType) method.getEnclosingElement().asType();
      ExecutableType et = (ExecutableType) types.asMemberOf(containing, method);
      List<? extends TypeMirror> paramTypes = et.getParameterTypes();
      if (paramTypes.isEmpty() || paramTypes.size() > 2) {
        // error(method, "wrong number of parameters, expecting exactly 1 or 2!");
        continue;
      }
      TypeMirror param0 = paramTypes.get(0);
      if (!types.isSameType(element(ByteBuffer.class).asType(), param0)) {
        // error(method, "expected %s as first parameter, but got: %s", ByteBuffer.class, param0);
        continue;
      }
      if (paramTypes.size() == 2) {
        stashs.put(Type.type(paramTypes.get(1)), method);
      } else {
        spawns.put(Type.type(et.getReturnType()), method);
      }
    }

    // symmetric difference
    Set<Type> intersection = new HashSet<>(spawns.keySet());
    intersection.retainAll(stashs.keySet());
    Set<Type> difference = new HashSet<>();
    difference.addAll(spawns.keySet());
    difference.addAll(stashs.keySet());
    difference.removeAll(intersection);
    if (!difference.isEmpty()) {
      for (Type singleton : difference) {
        ExecutableElement method = spawns.get(singleton);
        method = method == null ? stashs.get(singleton) : method;
        error(method, "dangling stashlet", method);
      }
    }

    for (Type type : intersection) {
      Name stashName = Name.name(stashs.get(type));
      Name spawnName = Name.name(spawns.get(type));
      stashlets.put(type, new StaticStashlet(type, stashName, spawnName));
    }
  }
}
