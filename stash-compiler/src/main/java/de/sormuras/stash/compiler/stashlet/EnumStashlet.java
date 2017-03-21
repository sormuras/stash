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

class EnumStashlet implements Stashlet<Enum> {

  private static final Name STASH = Name.reflect(Buffer.class, "stashEnum");
  private static final Name SPAWN = Name.reflect(Buffer.class, "spawnEnum");

  @Override
  public Type forType() {
    return Type.type(Enum.class);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public ByteBuffer stash(ByteBuffer buffer, Enum value) {
    return Buffer.stashEnum(buffer, value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Enum spawn(ByteBuffer source, Class<? extends Enum> type) {
    return Buffer.spawnEnum(source, type.getEnumConstants());
  }

  @Override
  public Listable stash(String TARGET, String name) {
    return listing -> listing.add(STASH).add("(" + TARGET + ", " + name + ")");
  }

  @Override
  public Listable spawn(String SOURCE, Type type) {
    return listing -> listing.add(SPAWN).eval("(" + SOURCE + ", {{T}}.values())", type);
  }
}
