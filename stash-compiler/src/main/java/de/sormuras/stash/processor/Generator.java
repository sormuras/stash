package de.sormuras.stash.processor;

import de.sormuras.beethoven.composer.ImportsComposer;
import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.beethoven.unit.FieldDeclaration;
import de.sormuras.beethoven.unit.InterfaceDeclaration;
import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.NormalClassDeclaration;
import de.sormuras.beethoven.unit.UnitTool;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Modifier;

public class Generator {

  protected final InterfaceDeclaration interfaceDeclaration;

  public Generator(InterfaceDeclaration interfaceDeclaration) {
    this.interfaceDeclaration = interfaceDeclaration;
  }

  public List<CompilationUnit> generate() {
    String packageName = interfaceDeclaration.getCompilationUnit().getPackageName();
    CompilationUnit stashUnit = generateStash(CompilationUnit.of(packageName));
    new ImportsComposer().apply(stashUnit);
    CompilationUnit guardUnit = generateGuard(CompilationUnit.of(packageName));
    return Arrays.asList(stashUnit, guardUnit);
  }

  // create compilation unit "XyzStash.java" with "class XyzStash implements Xyz {...}"
  CompilationUnit generateStash(CompilationUnit unit) {
    NormalClassDeclaration stashDeclaration =
        unit.declareClass(interfaceDeclaration.getName() + "Stash");
    stashDeclaration.setModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    stashDeclaration.addInterface(interfaceDeclaration.toType());

    FieldDeclaration other = stashDeclaration.declareField(interfaceDeclaration.toType(), "other");
    other.setModifiers(Modifier.PROTECTED, Modifier.FINAL);

    MethodDeclaration constructor = stashDeclaration.declareConstructor();
    constructor.declareParameter(interfaceDeclaration.toType(), "other");
    constructor.addStatement("this.other = other");

    for (MethodDeclaration interfaceMethod : interfaceDeclaration.getMethods()) {
      MethodDeclaration stashMethod =
          stashDeclaration.declareMethod(UnitTool.override(interfaceMethod, true));
      stashMethod.getModifiers().remove(Modifier.DEFAULT);

      stashMethod.addStatement(
          listing -> {
            if (!stashMethod.getReturnType().isVoid()) {
              listing.add("return ");
            }
            listing.add("other.");
            stashMethod.applyCall(listing);
            return listing;
          });
    }

    return unit;
  }

  // create compilation unit "XyzGuard.java" with "class XyzGuard implements Xyz"
  CompilationUnit generateGuard(CompilationUnit unit) {
    NormalClassDeclaration guardDeclaration =
        unit.declareClass(interfaceDeclaration.getName() + "Guard");
    guardDeclaration.setModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    guardDeclaration.addInterface(interfaceDeclaration.toType());
    return unit;
  }
}
