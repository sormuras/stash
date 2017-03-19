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

import de.sormuras.beethoven.Annotation;
import de.sormuras.beethoven.composer.ImportsComposer;
import de.sormuras.beethoven.unit.ClassDeclaration;
import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.beethoven.unit.InterfaceDeclaration;
import de.sormuras.stash.Stash;
import de.sormuras.stash.compiler.generator.StashGenerator;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;
import javax.annotation.Generated;
import javax.lang.model.element.Modifier;

public class Generator {

  private final InterfaceDeclaration interfaceDeclaration;
  private final Stash interfaceAnnotation;

  private final CRC32 crc32;
  private final Set<String> hashSet;
  private final Instant now;

  Generator(Stash interfaceAnnotation, InterfaceDeclaration interfaceDeclaration) {
    this.interfaceAnnotation = interfaceAnnotation;
    this.interfaceDeclaration = interfaceDeclaration;

    this.now = Instant.now();
    this.crc32 = new CRC32();
    this.hashSet = new HashSet<>();
  }

  public InterfaceDeclaration getInterfaceDeclaration() {
    return interfaceDeclaration;
  }

  protected Annotation buildAnnotationGenerated() {
    Annotation generated = Annotation.annotation(Generated.class);
    generated.addValue(getClass().getCanonicalName());
    generated.addValue(Stash.VERSION);
    generated.addObject("date", now.toString());
    String comments = interfaceAnnotation.comments();
    if (!comments.isEmpty()) {
      generated.addMember("comments", l -> l.add(comments));
    }
    return generated;
  }

  List<CompilationUnit> generate() {
    String packageName = interfaceDeclaration.getCompilationUnit().getPackageName();
    CompilationUnit stashUnit = generateStash(CompilationUnit.of(packageName));
    new ImportsComposer().apply(stashUnit);
    CompilationUnit guardUnit = generateGuard(CompilationUnit.of(packageName));
    return Arrays.asList(stashUnit, guardUnit);
  }

  // create compilation unit "DemoStash.java" with "class DemoStash implements Demo {...}"
  private CompilationUnit generateStash(CompilationUnit unit) {
    StashGenerator stashGenerator = new StashGenerator(this, unit);
    ClassDeclaration stashClass = stashGenerator.generate();
    stashClass.addAnnotation(buildAnnotationGenerated());
    return unit;
  }

  // create compilation unit "DemoGuard.java" with "class DemoGuard implements Demo {...}"
  private CompilationUnit generateGuard(CompilationUnit unit) {
    ClassDeclaration guardDeclaration = unit.declareClass(interfaceDeclaration.getName() + "Guard");
    guardDeclaration.setModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    guardDeclaration.addInterface(interfaceDeclaration.toType());
    return unit;
  }
}
