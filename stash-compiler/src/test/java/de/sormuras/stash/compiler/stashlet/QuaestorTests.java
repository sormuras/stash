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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.Style;
import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.N;
import de.sormuras.stash.compiler.Stashlet;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class QuaestorTests {

  private final Quaestor quaestor = new Quaestor();

  @TestFactory
  List<DynamicTest> eachStashlet() {
    List<DynamicTest> tests = new ArrayList<>();
    for (Stashlet<?> stashlet : quaestor) {
      Listing listing = new Listing(Style.SIMPLE);
      listing.add(stashlet.forType());
      String displayName = stashlet.getClass().getSimpleName() + "(" + listing + ")";
      tests.add(DynamicTest.dynamicTest(displayName, () -> testStashlet(stashlet)));
    }
    return tests;
  }

  private void testStashlet(Stashlet stashlet) {
    assertNotNull(stashlet);
    assertNotNull(stashlet.forType());
    assertNotNull(stashlet.spawn(stashlet.forType()));
    assertNotNull(stashlet.stash("value"));
  }

  @SafeVarargs
  private final Class<? extends Stashlet> stashletClass(
      Class<?> type, Class<? extends Annotation>... annotations) {
    Stashlet<?> stashlet = quaestor.resolve(type, annotations);
    return stashlet.getClass();
  }

  @Test
  void testGet() throws Exception {
    // primitives
    assertEquals(PrimitiveStashlet.class, stashletClass(byte.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(char.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(double.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(float.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(int.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(long.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(short.class));
    // wrappers
    assertEquals(PrimitiveStashlet.class, stashletClass(Byte.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(Character.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(Double.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(Float.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(Integer.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(Long.class));
    assertEquals(PrimitiveStashlet.class, stashletClass(Short.class));
    // simple arrays
    assertEquals(StaticStashlet.class, stashletClass(byte[].class));
    assertEquals(StaticStashlet.class, stashletClass(char[].class));
    assertEquals(StaticStashlet.class, stashletClass(double[].class));
    assertEquals(StaticStashlet.class, stashletClass(float[].class));
    assertEquals(StaticStashlet.class, stashletClass(int[].class));
    assertEquals(StaticStashlet.class, stashletClass(long[].class));
    assertEquals(StaticStashlet.class, stashletClass(short[].class));
    // more statics
    assertEquals(StaticStashlet.class, stashletClass(boolean.class));
    assertEquals(StaticStashlet.class, stashletClass(Boolean.class));
    assertEquals(StaticStashlet.class, stashletClass(int.class, N.class));
    assertEquals(StaticStashlet.class, stashletClass(long.class, N.class));
    // generic and fallback
    //assertEquals(StashableStashlet.class, stashletClass(Data.class));
    //assertEquals(StashableStashlet.class, stashletClass(Dataless.class));
    assertEquals(EnumStashlet.class, stashletClass(Thread.State.class));
    assertEquals(EnumStashlet.class, stashletClass(ProcessBuilder.Redirect.Type.class));
    assertEquals(EnumStashlet.class, stashletClass(Character.UnicodeScript.class));
    assertEquals(AnyStashlet.class, stashletClass(Error.class));
    assertEquals(AnyStashlet.class, stashletClass(Object.class));
    // loaded via service API
    // assertEquals(BigIntegerStashlet.class, stashletClass(BigInteger.class));
  }

  @Test
  void customMapIsMutable() {
    assertEquals(0, quaestor.customs().size());
    quaestor.customs().put(Type.type(getClass()), new AnyStashlet());
    assertEquals(1, quaestor.customs().size());
    assertEquals(AnyStashlet.class, stashletClass(getClass()));
    quaestor.customs().clear();
    assertEquals(0, quaestor.customs().size());
  }
}
