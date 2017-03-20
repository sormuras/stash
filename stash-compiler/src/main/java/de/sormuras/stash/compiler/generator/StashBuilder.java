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
import javax.lang.model.element.Modifier;

public class StashBuilder {

  final Generator generator;
  final CompilationUnit compilationUnit;

  final NormalClassDeclaration stashClass;
  final FieldDeclaration buffer;
  final FieldDeclaration other;

  public StashBuilder(Generator generator, CompilationUnit compilationUnit) {
    this.generator = generator;
    this.compilationUnit = compilationUnit;

    this.stashClass = createStashClass();
    this.buffer = createStashFieldBuffer();
    this.other = createStashFieldOther();
  }

  private NormalClassDeclaration createStashClass() {
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
    toString.addStatement("return this." + other.getName() + ".toString()");
  }

  private void generateMethods() {
    for (MethodDeclaration interfaceMethod : generator.getInterfaceDeclaration().getMethods()) {
      generateMethodImplementation(interfaceMethod);
      generateMethodRespawn(interfaceMethod);
    }
  }

  private void generateMethodImplementation(MethodDeclaration interfaceMethod) {
    MethodDeclaration method = stashClass.declareMethod(UnitTool.override(interfaceMethod, true));
    method.getModifiers().remove(Modifier.DEFAULT);

    method.addStatement(
        listing -> {
          if (!method.getReturnType().isVoid()) {
            listing.add("return ");
          }
          listing.add("this." + other.getName() + ".");
          method.applyCall(listing);
          return listing;
        });
  }

  private void generateMethodRespawn(MethodDeclaration interfaceMethod) {
    String hash = generator.buildMethodHash(interfaceMethod);
    String name = generator.buildSpawnMethodName(interfaceMethod, hash);
    MethodDeclaration method = stashClass.declareMethod(void.class, name);
    method.setModifiers(Modifier.PRIVATE);
    method.addStatement("// TODO");
  }
}
