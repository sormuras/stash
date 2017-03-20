package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
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

  @Test
  void store() {
    Demo.Impl impl = new Demo.Impl();
    assertEquals("[0, 0, 0, 0, 0] = 0", impl.toString());

    ByteBuffer buffer = ByteBuffer.allocate(1000);
    Demo demo = new DemoStash(impl, buffer);
    demo.store(1);
    demo.store(2);
    demo.store(3);
    assertEquals("[1, 2, 3, 0, 0] = 6", impl.toString());

    buffer.flip();

    Demo next = new DemoStash(new Demo.Impl(), buffer);
    next.store(4);
    assertEquals("[1, 2, 3, 4, 0] = 10", next.toString());
  }
}
