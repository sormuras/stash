package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DemoTests {

  @Test
  void generateDemoStash() {
    assertEquals("demo.DemoStash", DemoStash.class.getCanonicalName());
  }

  @Test
  void generateDemoGuard() {
    assertEquals("demo.DemoGuard", DemoGuard.class.getCanonicalName());
  }
}
