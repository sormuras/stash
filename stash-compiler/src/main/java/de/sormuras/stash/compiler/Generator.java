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
import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.composer.ImportsComposer;
import de.sormuras.beethoven.unit.ClassDeclaration;
import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.beethoven.unit.InterfaceDeclaration;
import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.MethodParameter;
import de.sormuras.stash.Stash;
import de.sormuras.stash.compiler.generator.StashBuilder;
import de.sormuras.stash.compiler.stashlet.Quaestor;
import de.sormuras.stash.compiler.stashlet.Query;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.CRC32;
import javax.annotation.Generated;
import javax.lang.model.element.Modifier;

public class Generator {

  private final InterfaceDeclaration interfaceDeclaration;
  private final Stash interfaceAnnotation;

  private final CRC32 crc32;
  private final Instant now;
  private final Quaestor quaestor;

  Generator(Stash interfaceAnnotation, InterfaceDeclaration interfaceDeclaration) {
    this.interfaceAnnotation = interfaceAnnotation;
    this.interfaceDeclaration = interfaceDeclaration;

    this.now = Instant.now();
    this.crc32 = new CRC32();
    this.quaestor = new Quaestor();
  }

  public InterfaceDeclaration getInterfaceDeclaration() {
    return interfaceDeclaration;
  }

  private Annotation buildAnnotationGenerated() {
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

  public String buildOtherName() {
    return interfaceDeclaration.getName().toLowerCase(); // .toCamelCase();
  }

  public String buildMethodHash(MethodDeclaration method) {
    crc32.reset();
    crc32.update(method.getName().getBytes());
    method.getParameters().forEach(p -> crc32.update(p.getType().list().getBytes()));
    String hash = Long.toHexString(crc32.getValue()).toUpperCase();
    hash = ("00000000" + hash).substring(hash.length());
    return "0x" + hash;
  }

  public String buildSpawnMethodName(MethodDeclaration method, String hash) {
    return method.getName() + hash;
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
    StashBuilder stashGenerator = new StashBuilder(this, unit);
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

  public Optional<MethodParameter> getTimeParameter(MethodDeclaration method) {
    return method.getParameters().stream().filter(this::isParameterTime).findFirst();
  }

  public boolean isMethodChainable(MethodDeclaration method) {
    return Boolean.TRUE.equals(method.getTags().get(Tag.METHOD_IS_CHAINABLE));
  }

  public boolean isMethodVolatile(MethodDeclaration method) {
    return Boolean.TRUE.equals(method.getTags().get(Tag.METHOD_IS_VOLATILE));
  }

  public boolean isMethodReturn(MethodDeclaration method) {
    return !method.getReturnType().isVoid();
  }

  public boolean isParameterTime(MethodParameter parameter) {
    return Boolean.TRUE.equals(parameter.getTags().get(Tag.PARAMETER_IS_TIME));
  }

  public boolean isVerify() {
    return interfaceAnnotation.verify();
  }

  public Stashlet<?> resolve(MethodParameter parameter) {
    boolean isStashable = false;
    boolean isEnum = false;
    if (parameter.isTagged()) {
      isStashable = Boolean.TRUE.equals(parameter.getTags().get(Tag.PARAMETER_IS_STASHABLE));
      isEnum = Boolean.TRUE.equals(parameter.getTags().get(Tag.PARAMETER_IS_ENUM));
    }
    Query query = new Query(parameter.getType(), isStashable, isEnum);
    return quaestor.resolve(query);
  }

  public Listing applyCall(Listing listing, MethodDeclaration method) {
    listing.add("this.");
    listing.add(buildOtherName());
    listing.add('.');
    method.applyCall(listing);
    listing.add(';');
    return listing.newline();
  }
}