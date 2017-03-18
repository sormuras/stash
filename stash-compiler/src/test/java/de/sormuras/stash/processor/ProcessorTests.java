package de.sormuras.stash.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.stash.Stash;
import org.junit.jupiter.api.Test;

class ProcessorTests {

  @Test
  void emptyInterfaceWithStashAnnotationCompiles() throws ClassNotFoundException {
    CompilationUnit terra = CompilationUnit.of("empty");
    terra.declareInterface("Empty").addAnnotation(Stash.class);
    Class<?> terraClass = terra.compile();
    assertEquals("empty.Empty", terraClass.getCanonicalName());
    ClassLoader loader = terraClass.getClassLoader();
    assertEquals("empty.EmptyGuard", loader.loadClass("empty.EmptyGuard").getName());
    assertEquals("empty.EmptyStash", loader.loadClass("empty.EmptyStash").getName());
  }
}
