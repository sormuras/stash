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
import de.sormuras.stash.compiler.Stashlet;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Call site: any static accessor method pair on specified call site.
 *
 * @param <T> type to handle
 */
public class StaticStashlet<T> extends AbstractStashlet<T> {

  @SuppressWarnings("unchecked")
  static <T> Stashlet<T> reflect(Class<?> container, Class<T> typeClass) {
    return (Stashlet<T>) reflect(container, Type.type(typeClass));
  }

  @SuppressWarnings("unchecked")
  static <T> Stashlet<T> reflect(Class<?> container, Type type) {
    return (Stashlet<T>) reflect(container).get(type);
  }

  static Map<Type, Stashlet<?>> reflect(Class<?> container) {
    Map<Type, Stashlet<?>> map = new HashMap<>();
    for (Method stashMethod : container.getDeclaredMethods()) {
      if (!Stashlet.StashFunction.matches(stashMethod)) {
        continue;
      }
      Type key = Type.type(stashMethod.getAnnotatedParameterTypes()[1]);
      if (key.isJavaLangObject()) {
        continue;
      }
      for (Method spawnMethod : container.getDeclaredMethods()) {
        if (!Stashlet.SpawnFunction.matches(spawnMethod)) {
          continue;
        }
        if (key.equals(Type.type(spawnMethod.getAnnotatedReturnType()))) {
          map.put(key, new StaticStashlet<>(key, container, stashMethod, spawnMethod));
          break;
        }
      }
    }
    return map;
  }

  private final Type container;

  private StaticStashlet(Type type, Class<?> container, Method stashMethod, Method spawnMethod) {
    super(type, stashMethod, spawnMethod);
    this.container = Type.type(container);
  }

  @Override
  public boolean equals(Object other) {
    return super.equals(other) && Objects.equals(container, ((StaticStashlet) other).container);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), container);
  }

  @Override
  public Listable stash(String target, String name) {
    return listing -> listing.add(getStashName()).add("(" + target + ", " + name + ")");
  }

  @Override
  public Listable spawn(String source, Type type) {
    return listing -> listing.add(getSpawnName()).add("(" + source + ")");
  }
}
