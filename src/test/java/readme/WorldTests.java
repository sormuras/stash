package readme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WorldTests {

  @Test
  void worldStashIsGenerated() {
    assertEquals("readme.WorldStash", WorldStash.class.getCanonicalName());
  }

  @Test
  void worldGuardIsGenerated() {
    assertEquals("readme.WorldGuard", WorldGuard.class.getCanonicalName());
  }
}
