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
import de.sormuras.stash.Stashable.Buffer;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class AnyStashletTests {

  private final AnyStashlet stashlet = new AnyStashlet();

  @Test
  void type() {
    assertEquals(Type.type(Object.class), stashlet.forType());
  }

  @Test
  void listable() {
    String container = Buffer.class.getCanonicalName();
    String expectedStash = container + ".stashAny(buffer, value)";
    String expectedSpawn = "(java.math.BigDecimal) " + container + ".spawnAny(buffer)";
    assertEquals(expectedStash, stashlet.stash("value").list());
    assertEquals(expectedSpawn, stashlet.spawn(Type.type(BigDecimal.class)).list());
  }

  @Test
  void runtime() {
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    BigDecimal expected = BigDecimal.TEN;
    stashlet.stash(buffer, expected);
    buffer.flip();
    BigDecimal actual = (BigDecimal) stashlet.spawn(buffer, BigDecimal.class);
    assertEquals(expected, actual);
  }
}
