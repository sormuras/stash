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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.Stashable.Buffer;
import de.sormuras.stash.compiler.Stashlet;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class StaticStashletTests {

  @Test
  void charArray() throws Exception {
    Class<?> container = Buffer.View.class;
    Stashlet<char[]> stashlet = StaticStashlet.reflect(container, char[].class);
    assertEquals(Type.type(char[].class), stashlet.forType());
    String name = container.getCanonicalName();
    assertEquals(name + ".stashCharArray(buffer, c)", stashlet.stash("c").list());
    assertEquals(name + ".spawnCharArray(buffer)", stashlet.spawn().list());
    char[] expected = {47, 11, 18, 0, Character.MAX_VALUE, Character.MIN_VALUE};
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    stashlet.stash(buffer, expected);
    buffer.flip();
    char[] actual = stashlet.spawn(buffer, char[].class);
    assertArrayEquals(expected, actual);
  }

  @Test
  void staticAndReflected() throws Exception {
    Type type = Type.withAnnotations(Type.type(long.class), de.sormuras.stash.N.class);
    @SuppressWarnings("unchecked")
    Stashlet<Long> stashlet = StaticStashlet.reflect(Buffer.class, type);
    assertEquals(type, stashlet.forType());
    String container = Buffer.class.getCanonicalName();
    assertEquals(container + ".stashLongN(buffer, l)", stashlet.stash("l").list());
    assertEquals(container + ".spawnLongN(buffer)", stashlet.spawn(type).list());
    Long expected = 127L;
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    stashlet.stash(buffer, expected);
    buffer.flip();
    assertEquals(1, buffer.remaining()); // not 8!
    Long actual = stashlet.spawn(buffer, Long.class);
    assertEquals(expected, actual);
  }
}
