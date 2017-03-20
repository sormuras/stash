/*
 * Copyright (C) 2017 Christian Stein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package demo;

import de.sormuras.stash.Stash;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Stash
public interface Demo {

  int store(int value);

  static void main(String... args) {
    int sum = 0;
    Impl impl = new Impl();
    System.out.println(Arrays.toString(impl.values) + " = " + sum); // [0, 0, 0, 0, 0] = 0

    ByteBuffer buffer = ByteBuffer.allocate(1000);
    Demo demo = new DemoStash(impl, buffer);
    demo.store(1);
    demo.store(2);
    sum = demo.store(3);
    System.out.println(Arrays.toString(impl.values) + " = " + sum); // [1, 2, 3, 0, 0] = 6

    impl = new Impl();
    buffer.flip();

    Demo next = new DemoStash(impl, buffer);
    sum = next.store(4);
    System.out.println(Arrays.toString(impl.values) + " = " + sum); // [1, 2, 3, 4, 0] = 10
  }

  class Impl implements Demo {

    private int index = 0;
    private final int[] values = new int[5];

    @Override
    public int store(int value) {
      values[index] = value;
      index = (index + 1) % values.length;
      return Arrays.stream(values).sum();
    }
  }
}
