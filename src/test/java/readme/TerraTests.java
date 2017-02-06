package readme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.beethoven.unit.CompilationUnit;
import de.sormuras.stash.Stash;
import org.junit.jupiter.api.Test;

class TerraTests {

  @Test
  void terraCompiles() throws ClassNotFoundException {
    CompilationUnit terra = CompilationUnit.of("planet");
    terra.declareInterface("Terra").addAnnotation(Stash.Interface.class);
    Class<?> terraClass = terra.compile();
    assertEquals("planet.Terra", terraClass.getCanonicalName());
    ClassLoader loader = terraClass.getClassLoader();
    assertEquals("planet.TerraGuard", loader.loadClass("planet.TerraGuard").getName());
    assertEquals("planet.TerraStash", loader.loadClass("planet.TerraStash").getName());
  }
}
