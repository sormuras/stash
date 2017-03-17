package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DemoTests {

  @Test
  void worldStashIsGenerated() {
    assertEquals("demo.DemoStash", DemoStash.class.getCanonicalName());
  }

  @Test
  void worldGuardIsGenerated() {
    assertEquals("demo.DemoGuard", DemoGuard.class.getCanonicalName());
  }
}
