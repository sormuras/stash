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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.Stashable;
import de.sormuras.stash.compiler.Stashlet;
import de.sormuras.stash.compiler.stashlet.Quaestor;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class BigIntegerStashletTests {

  @Test
  void bigIntegerStashlet() throws Exception {
    @SuppressWarnings("unchecked")
    Stashlet<BigInteger> stashlet = (Stashlet<BigInteger>) new Quaestor().resolve(BigInteger.class);
    // code snippets
    String container = Stashable.Buffer.class.getCanonicalName();
    assertEquals(
        container + ".stashByteArray(buffer, value.toByteArray())", stashlet.stash("value").list());
    assertEquals(
        "new java.math.BigInteger(" + container + ".spawnByteArray(buffer))",
        stashlet.spawn(Type.type(BigInteger.class)).list());
    // runtime
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    BigInteger expected = BigInteger.valueOf(132);
    stashlet.stash(buffer, expected);
    buffer.flip();
    BigInteger actual = stashlet.spawn(buffer, BigInteger.class);
    assertEquals(expected, actual);
  }
}
