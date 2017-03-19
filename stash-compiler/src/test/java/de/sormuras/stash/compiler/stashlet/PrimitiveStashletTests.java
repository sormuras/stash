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

package de.sormuras.stash.compiler.stashlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.beethoven.type.Type;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class PrimitiveStashletTests {

  private final PrimitiveStashlet<Integer> stashlet = PrimitiveStashlet.of(int.class);

  @Test
  void type() {
    assertEquals(Type.type(int.class), stashlet.forType());
  }

  @Test
  void listing() {
    assertEquals("buffer.putInt(i)", stashlet.stash("i").list());
    assertEquals("buffer.getInt()", stashlet.spawn().list());
  }

  @Test
  void runtime() {
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    Integer expected = 4711;
    stashlet.stash(buffer, expected);
    buffer.flip();
    assertEquals(4, buffer.remaining());
    Integer actual = stashlet.spawn(buffer, Integer.class);
    assertEquals(expected, actual);
  }
}
