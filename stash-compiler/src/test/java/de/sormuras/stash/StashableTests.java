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

package de.sormuras.stash;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class StashableTests {

  @Test
  void stashableIsStashable() {
    assertTrue(Stashable.class.isAssignableFrom(Data.class));
    assertTrue(Stashable.class.isAssignableFrom(Zero.class));
    assertTrue(Stashable.class.isAssignableFrom(MissingConstructor.class));
    assertTrue(Stashable.isStashable(Data.class));
    assertTrue(Stashable.isStashable(Zero.class));
    assertFalse(Stashable.isStashable(MissingConstructor.class));
    assertFalse(Stashable.isStashable(Object.class));
    assertFalse(Stashable.isStashable(null));
  }

  @Test
  void checkStashable() {
    Stashable.check(Data.class);
    Stashable.check(Zero.class);
  }

  @Test
  void checkInterfaceNotAllowed() {
    assertThrows(IllegalArgumentException.class, () -> Stashable.check(Stashable.class));
  }

  @Test
  void checkMissingConstructor() {
    assertThrows(IllegalArgumentException.class, () -> Stashable.check(MissingConstructor.class));
  }

  @Test
  void checkNotImplementing() {
    assertThrows(IllegalArgumentException.class, () -> Stashable.check(Object.class));
  }

  @Test
  void checkNull() {
    assertThrows(NullPointerException.class, () -> Stashable.check(null));
  }

  @TestFactory
  List<DynamicTest> stashable() throws Exception {
    return asList(
        dynamicTest("Data", () -> stashable(Data::new, Data::new, Long.BYTES)),
        dynamicTest("Zero", () -> stashable(Zero::new, Zero::new, 0)));
  }

  private <S extends Stashable> void stashable(
      Supplier<S> constructorWithoutParameters,
      Function<ByteBuffer, S> constructorWithByteBuffer,
      int expectedSize)
      throws Exception {
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    S expected = constructorWithoutParameters.get();
    expected.stash(buffer);
    assertEquals(expectedSize, buffer.position());
    buffer.flip();
    S actual = constructorWithByteBuffer.apply(buffer);
    assertEquals(expectedSize, buffer.position());
    assertEquals(expected.hashCode(), actual.hashCode(), "hashCode() mismatch");
  }

  static class MissingConstructor implements Stashable {

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      return target;
    }
  }
}
