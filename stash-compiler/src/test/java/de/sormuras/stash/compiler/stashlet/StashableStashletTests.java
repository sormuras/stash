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
import de.sormuras.stash.Stashable;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class StashableStashletTests {

  private final StashableStashlet stashlet = new StashableStashlet();

  @Test
  void type() {
    assertEquals(Type.type(Stashable.class), stashlet.forType());
  }

  @Test
  void listing() {
    String data = Data.class.getCanonicalName();
    assertEquals("data.stash(buffer)", stashlet.stash("data").list());
    assertEquals("new " + data + "(buffer)", stashlet.spawn(Type.type(Data.class)).list());
  }

  @Test
  void runtime() throws Exception {
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    Data expected = new Data();
    stashlet.stash(buffer, expected);
    buffer.flip();
    Data actual = (Data) stashlet.spawn(buffer, Data.class);
    assertEquals(expected.value, actual.value);
    // once again with explicit instantiation
    buffer.flip();
    assertEquals(expected.value, new Data(buffer).value);
  }

  public static class Data implements Stashable {

    private final long value;

    public Data() {
      this.value = Math.round(Math.random());
    }

    public Data(ByteBuffer source) {
      this.value = source.getLong();
    }

    @Override
    public int hashCode() {
      return (int) value;
    }

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      return target.putLong(value);
    }
  }
}
