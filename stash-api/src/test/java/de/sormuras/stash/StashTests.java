package de.sormuras.stash;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

@Stash.Interface
class StashTests {

  @Test
  void stashIsInterface() {
    assertTrue(Stash.class.isInterface());
  }

  @Test
  void defaultInterfaceAnnotationValues() {
    Stash.Interface annotation = StashTests.class.getAnnotation(Stash.Interface.class);
    assertNotNull(annotation);
    assertSame(Object.class, annotation.classExtends());
    assertTrue(annotation.verify());
  }
}
