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
import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.Stashable;
import de.sormuras.stash.compiler.Stashlet;
import java.nio.ByteBuffer;

class StashableStashlet implements Stashlet<Stashable> {

  @Override
  public Type forType() {
    return Type.type(Stashable.class);
  }

  @Override
  public <S extends Stashable> ByteBuffer stash(ByteBuffer target, S stashable) {
    return stashable.stash(target);
  }

  @Override
  public Stashable spawn(ByteBuffer source, Class<? extends Stashable> type) throws Exception {
    return type.getConstructor(ByteBuffer.class).newInstance(source);
  }

  @Override
  public Listable spawn(String source, Type type) {
    return listing -> listing.eval("new {{T}}({{$}})", type, source);
  }

  @Override
  public Listable stash(String target, String name) {
    return listing -> listing.eval("{{$}}.stash({{$}})", name, target);
  }
}
