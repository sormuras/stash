package de.sormuras.stash.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.beethoven.type.Type;
import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.beethoven.unit.InterfaceDeclaration;
import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.TypeDeclaration;
import de.sormuras.stash.Stash;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.Test;

class ProcessorTests {

  private void assertCompiles(CompilationUnit unit) throws ClassNotFoundException {
    TypeDeclaration typeDeclaration = unit.getEponymousDeclaration().orElseThrow(Error::new);
    String interfaceName = unit.getPackageName() + "." + typeDeclaration.getName();
    Class<?> interfaceClass = unit.compile();
    assertEquals(interfaceName, interfaceClass.getCanonicalName());
    ClassLoader loader = interfaceClass.getClassLoader();
    assertEquals(interfaceName + "Guard", loader.loadClass(interfaceName + "Guard").getName());
    assertEquals(interfaceName + "Stash", loader.loadClass(interfaceName + "Stash").getName());
  }

  @Test
  void emptyInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit unit = CompilationUnit.of("empty");
    unit.declareInterface("Empty").addAnnotation(Stash.class);
    assertCompiles(unit);
  }

  @Test
  void functionalInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit unit = CompilationUnit.of("single");
    InterfaceDeclaration single = unit.declareInterface("Single");
    single.addAnnotation(Stash.class);
    single.addInterface(Type.type(Runnable.class));
    assertCompiles(unit);
  }

  @Test
  void onlyStaticMainInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit unit = CompilationUnit.of("main");
    InterfaceDeclaration single = unit.declareInterface("Main");
    single.addAnnotation(Stash.class);
    MethodDeclaration main = single.declareMethod(void.class, "main", Modifier.STATIC);
    main.addStatement("System.gc()");
    assertCompiles(unit);
  }
}
