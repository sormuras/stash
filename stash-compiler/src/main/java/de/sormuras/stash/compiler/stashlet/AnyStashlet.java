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

import de.sormuras.beethoven.Listable;
import de.sormuras.beethoven.Name;
import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.Stashable.Buffer;
import de.sormuras.stash.compiler.Stashlet;
import java.nio.ByteBuffer;

class AnyStashlet implements Stashlet<Object> {

  private static final Name STASH = Name.reflect(Buffer.class, "stashAny");
  private static final Name SPAWN = Name.reflect(Buffer.class, "spawnAny");

  @Override
  public Type forType() {
    return Type.type(Object.class);
  }

  @Override
  public ByteBuffer stash(ByteBuffer target, Object instance) {
    return Buffer.stashAny(target, instance);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object spawn(ByteBuffer source, Class<?> type) {
    return Buffer.spawnAny(source);
  }

  @Override
  public Listable stash(String TARGET, String name) {
    return listing -> listing.add(STASH).add("(" + TARGET + ", " + name + ")");
  }

  @Override
  public Listable spawn(String SOURCE, Type type) {
    Listable cast = listing -> listing.add('(').add(type).add(')');
    Listable call = listing -> listing.add(SPAWN).add('(').add(SOURCE).add(')');
    return listing -> listing.add(cast).add(' ').add(call);
  }
}
