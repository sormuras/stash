package de.sormuras.stash;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

@Stash
class StashTests {

  @Test
  void stashIsAnnotation() {
    assertTrue(Stash.class.isAnnotation());
  }

  @Test
  void defaultStashAnnotationValues() {
    Stash annotation = StashTests.class.getAnnotation(Stash.class);
    assertNotNull(annotation);
    assertSame(Object.class, annotation.classExtends());
    assertTrue(annotation.verify());
  }
}
