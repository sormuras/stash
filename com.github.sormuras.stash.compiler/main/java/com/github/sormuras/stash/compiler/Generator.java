package com.github.sormuras.stash.compiler;

import com.github.sormuras.beethoven.Annotation;
import com.github.sormuras.beethoven.Listing;
import com.github.sormuras.beethoven.type.ClassType;
import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.beethoven.unit.ClassDeclaration;
import com.github.sormuras.beethoven.unit.CompilationUnit;
import com.github.sormuras.beethoven.unit.InterfaceDeclaration;
import com.github.sormuras.beethoven.unit.MethodDeclaration;
import com.github.sormuras.beethoven.unit.MethodParameter;
import com.github.sormuras.stash.Stash;
import com.github.sormuras.stash.compiler.generator.StashBuilder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.CRC32;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;

public class Generator {

  private final Stash stash;
  private final InterfaceDeclaration declaration;

  private final CRC32 crc32;
  private final Instant now;
  private final InterfaceDeclaration io;
  private final Quaestor quaestor;

  Generator(ClassLoader loader, Stash stash, InterfaceDeclaration declaration) {
    this.stash = stash;
    this.declaration = declaration;

    this.crc32 = new CRC32();
    this.now = Instant.now();
    this.io = generateIO();
    this.quaestor = new Quaestor(this, loader);
  }

  public InterfaceDeclaration getInterfaceDeclaration() {
    return declaration;
  }

  public InterfaceDeclaration getIo() {
    return io;
  }

  public Quaestor getQuaestor() {
    return quaestor;
  }

  private Annotation buildAnnotationGenerated() {
    Annotation generated = Annotation.annotation(Deprecated.class);
    generated.addValue(getClass().getCanonicalName());
    generated.addValue(Stash.class.getModule().getDescriptor().toNameAndVersion());
    generated.addObject("date", now.toString());
    String comments = stash.comments();
    if (!comments.isEmpty()) {
      generated.addMember("comments", l -> l.add(comments));
    }
    return generated;
  }

  public ClassType buildSuperClass() {
    try {
      return ClassType.type(stash.classExtends());
    } catch (MirroredTypeException mte) {
      return (ClassType) Type.type(mte.getTypeMirror());
    }
  }

  public String buildOtherName() {
    return declaration.getName().toLowerCase(); // .toCamelCase();
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

  public Optional<MethodParameter> findTimeParameter(MethodDeclaration method) {
    return method.getParameters().stream().filter(Tag::isParameterTime).findFirst();
  }

  List<CompilationUnit> generate() {
    String packageName = declaration.getCompilationUnit().getPackageName();
    List<CompilationUnit> units = new ArrayList<>();
    units.add(generateStash(CompilationUnit.of(packageName)));
    units.add(generateGuard(CompilationUnit.of(packageName)));
    units.add(io.getCompilationUnit());
    return units;
  }

  // create compilation unit "DemoStash.java" with "class DemoStash implements Demo {...}"
  private CompilationUnit generateStash(CompilationUnit unit) {
    StashBuilder stashBuilder = new StashBuilder(this, unit);
    ClassDeclaration stashClass = stashBuilder.generate();
    // stashClass.addAnnotation(buildAnnotationGenerated());
    return unit;
  }

  // create compilation unit "DemoGuard.java" with "class DemoGuard implements Demo {...}"
  private CompilationUnit generateGuard(CompilationUnit unit) {
    ClassDeclaration guardDeclaration = unit.declareClass(declaration.getName() + "Guard");
    guardDeclaration.setModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    guardDeclaration.addInterface(declaration.toType());
    // guardDeclaration.addAnnotation(buildAnnotationGenerated());
    return unit;
  }

  // create compilation unit "DemoIO.java" with "interface DemoIO {...}"
  private InterfaceDeclaration generateIO() {
    String packageName = declaration.getCompilationUnit().getPackageName();
    CompilationUnit unit = CompilationUnit.of(packageName);
    InterfaceDeclaration ioDeclaration = unit.declareInterface(declaration.getName() + "IO");
    // ioDeclaration.addAnnotation(buildAnnotationGenerated());
    return ioDeclaration;
  }

  public boolean isVerify() {
    return stash.verify();
  }

  public Stashlet resolve(Type type) {
    return quaestor.resolve(type);
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
