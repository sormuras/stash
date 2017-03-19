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

import static de.sormuras.beethoven.type.PrimitiveType.primitive;

import de.sormuras.beethoven.Listable;
import de.sormuras.beethoven.type.Type;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Call site: instance of java.nio.ByteBuffer.
 *
 * @param <T> primitive type to handle
 */
class PrimitiveStashlet<T> extends AbstractStashlet<T> {

  public static <T> PrimitiveStashlet<T> of(Class<T> type) {
    String suffix = type.getName();
    suffix = type == byte.class ? "" : suffix.substring(0, 1).toUpperCase() + suffix.substring(1);
    try {
      Method stashMethod = ByteBuffer.class.getMethod("put" + suffix, type);
      Method spawnMethod = ByteBuffer.class.getMethod("get" + suffix);
      return new PrimitiveStashlet<>(primitive(type), stashMethod, spawnMethod);
    } catch (NoSuchMethodException exception) {
      throw new AssertionError("Accessor for " + type + " not found!", exception);
    }
  }

  private PrimitiveStashlet(Type type, Method stashMethod, Method spawnMethod) {
    super(type, stashMethod, spawnMethod);
  }

  @Override
  public Listable spawn(String source, Type type) {
    return listing -> listing.add(source + "." + spawnName.lastName() + "()");
  }

  @Override
  public Listable stash(String target, String name) {
    return listing -> listing.add(target + "." + stashName.lastName() + "(" + name + ")");
  }
}
