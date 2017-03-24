package demo;

import de.sormuras.stash.Stash;
import de.sormuras.stash.Volatile;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Stash
public interface All {

  static void main(String... args) {
    System.out.println(AllStash.class);
    System.out.println(AllGuard.class);
  }

  default void enums(Thread.State state, Character.UnicodeScript script) {}

  default void primitive(boolean value, Boolean boxed) {}

  default void primitive(byte value, Byte boxed) {}

  default void primitive(char value, Character boxed) {}

  default void primitive(double value, Double boxed) {}

  default void primitive(float value, Float boxed) {}

  default void primitive(int value, Integer boxed) {}

  default void primitive(long value, Long boxed) {}

  default void primitive(short value, Short boxed) {}

  default void uuid(UUID uuid) {}

  default void stashable() {}

  default void string(String string, CharSequence sequence) {}

  @Volatile
  default List<Long> volatileTimes() {
    return Collections.emptyList();
  }

  static UUID createUUID(ByteBuffer source) {
    return new UUID(source.getLong(), source.getLong());
  }

  static ByteBuffer writeUUID(ByteBuffer target, UUID uuid) {
    return target.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
  }
}