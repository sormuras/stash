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

import de.sormuras.beethoven.Name;
import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.compiler.Stashlet;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Objects;

abstract class AbstractStashlet<T> implements Stashlet<T> {

  private final Type type;
  final Name stashName;
  final Name spawnName;
  private final StashFunction<T> stashFunction;
  private final SpawnFunction<T> spawnFunction;

  AbstractStashlet(Type type, Method stashMethod, Method spawnMethod) {
    this.type = type;
    this.stashName = Name.name(stashMethod);
    this.spawnName = Name.name(spawnMethod);
    this.stashFunction = StashFunction.of(stashMethod);
    this.spawnFunction = SpawnFunction.of(spawnMethod);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    AbstractStashlet<?> that = (AbstractStashlet<?>) other;
    return Objects.equals(type, that.type)
        && Objects.equals(stashName, that.stashName)
        && Objects.equals(spawnName, that.spawnName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, stashName, spawnName);
  }

  @Override
  public Type forType() {
    return type;
  }

  @Override
  public <V extends T> ByteBuffer stash(ByteBuffer target, V value) {
    return stashFunction.apply(target, value);
  }

  @Override
  public T spawn(ByteBuffer source, Class<? extends T> type) {
    return spawnFunction.apply(source);
  }
}
