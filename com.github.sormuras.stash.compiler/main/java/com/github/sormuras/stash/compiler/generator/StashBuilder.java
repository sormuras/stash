package com.github.sormuras.stash.compiler.generator;

import static com.github.sormuras.stash.compiler.Tag.isMethodVolatile;

import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.beethoven.unit.CompilationUnit;
import com.github.sormuras.beethoven.unit.FieldDeclaration;
import com.github.sormuras.beethoven.unit.MethodDeclaration;
import com.github.sormuras.beethoven.unit.NormalClassDeclaration;
import com.github.sormuras.beethoven.unit.UnitTool;
import com.github.sormuras.stash.compiler.Generator;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;

public class StashBuilder {

  final Generator generator;

  final NormalClassDeclaration stashClass;
  final FieldDeclaration buffer;
  final FieldDeclaration counter;
  final FieldDeclaration clock;
  final FieldDeclaration other;

  public StashBuilder(Generator generator, CompilationUnit compilationUnit) {
    this.generator = generator;

    this.stashClass = createStashClass(compilationUnit);
    this.counter = createStashFieldCounter();
    this.buffer = createStashFieldBuffer();
    this.clock = createStashFieldClock();
    this.other = createStashFieldOther();
  }

  private NormalClassDeclaration createStashClass(CompilationUnit compilationUnit) {
    String interfaceName = generator.getInterfaceDeclaration().getName();
    NormalClassDeclaration stashClass = compilationUnit.declareClass(interfaceName + "Stash");
    stashClass.setModifiers(Modifier.PUBLIC);
    stashClass.addInterface(generator.getInterfaceDeclaration().toType());
    stashClass.setSuperClass(generator.buildSuperClass());
    return stashClass;
  }

  private FieldDeclaration createStashFieldBuffer() {
    FieldDeclaration buffer = stashClass.declareField(ByteBuffer.class, "buffer");
    buffer.setModifiers(Modifier.PRIVATE, Modifier.FINAL);
    return buffer;
  }

  private FieldDeclaration createStashFieldCounter() {
    FieldDeclaration buffer = stashClass.declareField(long.class, "counter");
    buffer.setModifiers(Modifier.PRIVATE);
    return buffer;
  }

  private FieldDeclaration createStashFieldClock() {
    FieldDeclaration buffer = stashClass.declareField(Clock.class, "clock");
    buffer.setModifiers(Modifier.PRIVATE, Modifier.FINAL);
    return buffer;
  }

  private FieldDeclaration createStashFieldOther() {
    Type otherType = generator.getInterfaceDeclaration().toType();
    String otherName = generator.buildOtherName();
    FieldDeclaration other = stashClass.declareField(otherType, otherName);
    other.setModifiers(Modifier.PRIVATE, Modifier.FINAL);
    return other;
  }

  public NormalClassDeclaration generate() {
    generateConstructor();
    generateToString();
    generateMethods();
    return stashClass;
  }

  private void generateConstructor() {
    stashClass.declareMethod(new StashConstructor(this));
  }

  private void generateToString() {
    MethodDeclaration toString = stashClass.declareMethod(String.class, "toString");
    toString.addAnnotation(Override.class);
    toString.setModifiers(Modifier.PUBLIC);
    toString.addStatement("return this.{{$}}.toString()", other.getName());
  }

  private void generateMethods() {
    Set<String> hashes = new HashSet<>();
    for (MethodDeclaration interfaceMethod : generator.getInterfaceDeclaration().getMethods()) {
      String hash = generator.buildMethodHash(interfaceMethod);
      if (!hashes.add(hash)) {
        throw new AssertionError("Method hash collision: " + interfaceMethod);
      }
      generateMethodImplementation(interfaceMethod, hash);
      if (isMethodVolatile(interfaceMethod)) {
        continue;
      }
      generateMethodRespawn(interfaceMethod, hash);
    }
  }

  private void generateMethodImplementation(MethodDeclaration interfaceMethod, String hash) {
    MethodDeclaration method = stashClass.declareMethod(UnitTool.override(interfaceMethod, true));
    method.getModifiers().remove(Modifier.DEFAULT);
    method.setBody(new StashImplementationMethodBlock(this, interfaceMethod, hash));
  }

  private void generateMethodRespawn(MethodDeclaration interfaceMethod, String hash) {
    String name = generator.buildSpawnMethodName(interfaceMethod, hash);
    MethodDeclaration method = stashClass.declareMethod(interfaceMethod.getReturnType(), name);
    method.setModifiers(Modifier.PRIVATE);
    method.setBody(new StashSpawnMethodBlock(this, interfaceMethod));
  }
}
