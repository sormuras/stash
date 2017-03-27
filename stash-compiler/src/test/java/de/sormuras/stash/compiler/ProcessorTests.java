package de.sormuras.stash.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import de.sormuras.beethoven.type.Type;
import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.beethoven.unit.InterfaceDeclaration;
import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.TypeDeclaration;
import de.sormuras.stash.Stash;
import java.util.Random;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.Test;

class ProcessorTests {

  private void assertCompiles(CompilationUnit unit) throws ClassNotFoundException {
    assertCompiles(unit, true);
  }

  private void assertCompiles(CompilationUnit unit, boolean ioPresent)
      throws ClassNotFoundException {
    TypeDeclaration typeDeclaration = unit.getEponymousDeclaration().orElseThrow(Error::new);
    String interfaceName = unit.getPackageName() + "." + typeDeclaration.getName();
    Class<?> interfaceClass = unit.compile();
    assertEquals(interfaceName, interfaceClass.getCanonicalName());
    ClassLoader loader = interfaceClass.getClassLoader();
    // assertEquals(interfaceName + "Guard", loader.loadClass(interfaceName + "Guard").getName());
    assertEquals(interfaceName + "Stash", loader.loadClass(interfaceName + "Stash").getName());
    try {
      assertEquals(interfaceName + "IO", loader.loadClass(interfaceName + "IO").getName());
    } catch (ClassNotFoundException exception) {
      if (ioPresent) {
        fail("expected IO to be present:" + exception.getMessage());
      }
    }
  }

  @Test
  void emptyInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit unit = CompilationUnit.of("empty");
    unit.declareInterface("Empty").addAnnotation(Stash.class);
    assertCompiles(unit, false); // no method, no io
  }

  @Test
  void functionalInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit unit = CompilationUnit.of("single");
    InterfaceDeclaration single = unit.declareInterface("Single");
    single.addAnnotation(Stash.class);
    single.addInterface(Type.type(Runnable.class));
    assertCompiles(unit, false); // no parameter, no io
  }

  @Test
  void onlyStaticMainInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit unit = CompilationUnit.of("main");
    InterfaceDeclaration single = unit.declareInterface("Main");
    single.addAnnotation(Stash.class);
    MethodDeclaration main = single.declareMethod(void.class, "main", Modifier.STATIC);
    main.addStatement("System.gc()");
    assertCompiles(unit, false); // only static, no io
  }

  @Test
  void randomPropertyInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit unit = CompilationUnit.of("random");
    InterfaceDeclaration random = unit.declareInterface("Random");
    random.addAnnotation(Stash.class);
    random.declareMethod(void.class, "setRandom").declareParameter(Random.class, "random");
    random.declareMethod(Random.class, "getRandom");
    assertCompiles(unit);
  }

  @Test
  void uuidPropertyInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit unit = CompilationUnit.of("service");
    InterfaceDeclaration random = unit.declareInterface("Service");
    random.addAnnotation(Stash.class);
    random.declareMethod(void.class, "setUUID").declareParameter(UUID.class, "uuid");
    random.declareMethod(UUID.class, "getUUID");
    assertCompiles(unit, true);
  }
}
