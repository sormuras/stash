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

import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.compiler.Stashlet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class Quaestor implements Iterable<Stashlet> {

  private static Map<Type, Stashlet<?>> mapBasics() {
    Map<Type, Stashlet<?>> map = new LinkedHashMap<>();
    // primitives
    map.put(Type.type(byte.class), PrimitiveStashlet.of(byte.class));
    map.put(Type.type(char.class), PrimitiveStashlet.of(char.class));
    map.put(Type.type(double.class), PrimitiveStashlet.of(double.class));
    map.put(Type.type(float.class), PrimitiveStashlet.of(float.class));
    map.put(Type.type(int.class), PrimitiveStashlet.of(int.class));
    map.put(Type.type(long.class), PrimitiveStashlet.of(long.class));
    map.put(Type.type(short.class), PrimitiveStashlet.of(short.class));
    // box
    map.put(Type.type(Byte.class), map.get(Type.type(byte.class)));
    map.put(Type.type(Character.class), map.get(Type.type(char.class)));
    map.put(Type.type(Double.class), map.get(Type.type(double.class)));
    map.put(Type.type(Float.class), map.get(Type.type(float.class)));
    map.put(Type.type(Integer.class), map.get(Type.type(int.class)));
    map.put(Type.type(Long.class), map.get(Type.type(long.class)));
    map.put(Type.type(Short.class), map.get(Type.type(short.class)));
    // Scan Buffer.class
    // map.putAll(StaticStashlet.reflect(Buffer.class));
    // map.put(Type.type(Boolean.class), map.get(Type.type(boolean.class)));
    return map;
  }

  private static Map<Type, Stashlet<?>> mapServices() {
    Map<Type, Stashlet<?>> map = new LinkedHashMap<>();
    for (Stashlet stashlet : ServiceLoader.load(Stashlet.class)) {
      map.put(stashlet.forType(), stashlet);
    }
    return map;
  }

  private final Map<Type, Stashlet<?>> basics;
  private final Map<Type, Stashlet<?>> customs;
  private final Map<Type, Stashlet<?>> services;
  //private final Stashlet stashAny;
  //private final Stashlet stashEnum;
  private final Stashlet stashStashable;

  public Quaestor() {
    this.customs = new LinkedHashMap<>();
    this.basics = mapBasics();
    this.services = mapServices();
    //this.stashAny = new AnyStashlet();
    //this.stashEnum = new EnumStashlet();
    this.stashStashable = new StashableStashlet();
  }

  public Stashlet resolve(Query query) {
    Type type = query.getType();
    if (customs.containsKey(type)) return customs.get(type);
    if (services.containsKey(type)) return services.get(type);
    if (basics.containsKey(type)) return basics.get(type);
    if (query.isStashable()) return stashStashable;
    //if (query.isEnum()) return stashEnum;
    //return stashAny;
    throw new AssertionError("Could not resolve type: " + type);
  }

  public Map<Type, Stashlet<?>> customs() {
    return customs;
  }

  @Override
  public Iterator<Stashlet> iterator() {
    ArrayList<Stashlet> all = new ArrayList<>();
    all.addAll(customs.values());
    all.addAll(services.values());
    all.addAll(new HashSet<>(basics.values()));
    all.add(stashStashable);
    //all.add(stashEnum);
    //all.add(stashAny);
    return all.listIterator();
  }
}
