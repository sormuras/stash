package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AllTests implements All {

  @Test
  void date() {
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    All all = new AllStash(this, buffer);
    long start = buffer.position();
    all.date(new Date());
    long actual = buffer.position() - start;
    assertEquals(4 + 8, actual); // int + long
  }

  @Test
  void uuid() {
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    All all = new AllStash(this, buffer);
    long start = buffer.position();
    all.uuid(UUID.randomUUID());
    long actual = buffer.position() - start;
    assertEquals(4 + 8 + 8, actual); // int + two longs
  }
}
