package de.sormuras.stash.compiler;

import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.compiler.stashlet.AnyStashlet;
import de.sormuras.stash.compiler.stashlet.PrimitiveStashlet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

class Quaestor {

  private final Generator generator;
  private final Map<Type, Stashlet> cache;

  private final AnyStashlet anyStashlet;
  private final Map<Type, Stashlet> basics;
  private final Map<Type, Stashlet> customs;
  private final Map<Type, Stashlet> services;

  Quaestor(Generator generator) {
    this.generator = generator;
    this.cache = new HashMap<>();

    this.anyStashlet = new AnyStashlet();
    this.customs = new LinkedHashMap<>();
    this.basics = mapBasics();
    this.services = mapServices();
  }

  Stashlet resolve(Type type) {
    return cache.computeIfAbsent(type, this::computeStashlet);
  }

  private Stashlet computeStashlet(Type type) {
    Stashlet stashlet = lookupStashlet(type);
    stashlet.init(type, generator);
    return stashlet;
  }

  private Stashlet lookupStashlet(Type type) {
    if (customs.containsKey(type)) return customs.get(type);
    if (services.containsKey(type)) return services.get(type);
    if (basics.containsKey(type)) return basics.get(type);
    // if (query.isStashable()) return stashStashable;
    // if (query.isEnum()) return stashEnum;
    return anyStashlet;
  }

  private static Map<Type, Stashlet> mapBasics() {
    Map<Type, Stashlet> map = new LinkedHashMap<>();
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
    return map;
  }

  private static Map<Type, Stashlet> mapServices() {
    Map<Type, Stashlet> map = new LinkedHashMap<>();
    for (Stashlet stashlet : ServiceLoader.load(Stashlet.class)) {
      map.put(stashlet.forType(), stashlet);
    }
    return map;
  }
}
