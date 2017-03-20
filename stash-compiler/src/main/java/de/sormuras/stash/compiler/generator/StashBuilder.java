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

package de.sormuras.stash.compiler.generator;

import de.sormuras.beethoven.type.Type;
import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.beethoven.unit.FieldDeclaration;
import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.NormalClassDeclaration;
import de.sormuras.beethoven.unit.UnitTool;
import de.sormuras.stash.compiler.Generator;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;

public class StashBuilder {

  final Generator generator;

  final NormalClassDeclaration stashClass;
  final FieldDeclaration buffer;
  final FieldDeclaration counter;
  final FieldDeclaration other;

  public StashBuilder(Generator generator, CompilationUnit compilationUnit) {
    this.generator = generator;

    this.stashClass = createStashClass(compilationUnit);
    this.counter = createStashFieldCounter();
    this.buffer = createStashFieldBuffer();
    this.other = createStashFieldOther();
  }

  private NormalClassDeclaration createStashClass(CompilationUnit compilationUnit) {
    String interfaceName = generator.getInterfaceDeclaration().getName();
    NormalClassDeclaration stashClass = compilationUnit.declareClass(interfaceName + "Stash");
    stashClass.setModifiers(Modifier.PUBLIC);
    stashClass.addInterface(generator.getInterfaceDeclaration().toType());
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
      if (generator.isMethodVolatile(interfaceMethod)) {
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
