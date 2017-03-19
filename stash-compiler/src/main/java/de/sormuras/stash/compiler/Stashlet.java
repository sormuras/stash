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

package de.sormuras.stash.compiler;

import static java.lang.invoke.MethodHandleProxies.asInterfaceInstance;
import static java.lang.invoke.MethodHandles.publicLookup;

import de.sormuras.beethoven.Listable;
import de.sormuras.beethoven.type.Type;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stashlet<T> {

  @FunctionalInterface
  interface StashFunction<T> extends BiFunction<ByteBuffer, T, ByteBuffer> {

    static boolean matches(Method method) {
      if (!method.getReturnType().equals(ByteBuffer.class)) {
        return false;
      }
      if (method.getParameterCount() != 2) {
        return false;
      }
      return method.getParameterTypes()[0].equals(ByteBuffer.class);
    }

    @SuppressWarnings("unchecked")
    static <T> StashFunction<T> of(Method method) {
      try {
        return asInterfaceInstance(StashFunction.class, publicLookup().unreflect(method));
      } catch (ReflectiveOperationException exception) {
        throw new AssertionError("Creating StashFunction instance failed!", exception);
      }
    }
  }

  @FunctionalInterface
  interface SpawnFunction<T> extends Function<ByteBuffer, T> {

    static boolean matches(Method method) {
      if (method.getReturnType().equals(void.class)) {
        return false;
      }
      if (method.getParameterCount() != 1) {
        return false;
      }
      return method.getParameterTypes()[0].equals(ByteBuffer.class);
    }

    @SuppressWarnings("unchecked")
    static <T> SpawnFunction<T> of(Method method) {
      try {
        return asInterfaceInstance(SpawnFunction.class, publicLookup().unreflect(method));
      } catch (ReflectiveOperationException exception) {
        throw new AssertionError("Creating SpawnFunction instance failed!", exception);
      }
    }
  }

  Type forType();

  // proxy

  T spawn(ByteBuffer source, Class<? extends T> type) throws Exception;

  <V extends T> ByteBuffer stash(ByteBuffer target, V value);

  // source generation

  default Listable spawn() {
    return spawn(forType());
  }

  default Listable spawn(Type type) {
    return spawn("buffer", type);
  }

  Listable spawn(String source, Type type);

  default Listable stash(String name) {
    return stash("buffer", name);
  }

  Listable stash(String target, String name);
}
