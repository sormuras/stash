package demo;

import de.sormuras.stash.Stash;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Stash
public interface Demo {

  int store(int value);

  static void main(String... args) {
    Impl impl = new Impl();
    System.out.println(impl); // [0, 0, 0, 0, 0] = 0

    ByteBuffer buffer = ByteBuffer.allocate(1000);
    Demo demo = new DemoStash(impl, buffer);
    demo.store(1);
    demo.store(2);
    demo.store(3);
    System.out.println(impl); // [1, 2, 3, 0, 0] = 6

    buffer.flip();

    Demo next = new DemoStash(new Impl(), buffer);
    next.store(4);
    System.out.println(next); // [1, 2, 3, 4, 0] = 10
  }

  class Impl implements Demo {

    private int index = 0;
    final int[] values = new int[5];

    @Override
    public int store(int value) {
      values[index] = value;
      index = (index + 1) % values.length;
      return sum();
    }

    private int sum() {
      return Arrays.stream(values).sum();
    }

    @Override
    public String toString() {
      return Arrays.toString(values) + " = " + sum();
    }
  }
}
