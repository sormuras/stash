package com.github.sormuras.stash.compiler;

import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.stash.compiler.stashlet.AnyStashlet;
import com.github.sormuras.stash.compiler.stashlet.BooleanStashlet;
import com.github.sormuras.stash.compiler.stashlet.EnumStashlet;
import com.github.sormuras.stash.compiler.stashlet.PrimitiveStashlet;
import com.github.sormuras.stash.compiler.stashlet.StashableStashlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class Quaestor implements Iterable<Stashlet> {

  private final Generator generator;
  private final Map<Type, Stashlet> cache;

  private final AnyStashlet anyStashlet;
  private final EnumStashlet enumStashlet;
  private final StashableStashlet stashableStashlet;
  private final Map<Type, Stashlet> basics;
  private final Map<Type, Stashlet> customs;
  private final Map<Type, Stashlet> services;

  Quaestor(Generator generator, ClassLoader loader) {
    this.generator = generator;
    this.cache = new HashMap<>();

    this.customs = new LinkedHashMap<>();
    this.basics = mapBasics();
    this.services = mapServices(loader);
    this.stashableStashlet = new StashableStashlet();
    this.enumStashlet = new EnumStashlet();
    this.anyStashlet = new AnyStashlet();
  }

  public Map<Type, Stashlet> getCustoms() {
    return customs;
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
    if (Tag.isTypeStashable(type)) return stashableStashlet;
    if (Tag.isTypeEnum(type)) return enumStashlet;
    return anyStashlet;
  }

  private List<Stashlet> getAllStashlets() {
    List<Stashlet> all = new ArrayList<>();
    all.addAll(customs.values());
    all.addAll(services.values());
    all.addAll(new HashSet<>(basics.values()));
    all.add(stashableStashlet);
    all.add(enumStashlet);
    all.add(anyStashlet);
    return all;
  }

  @Override
  public Iterator<Stashlet> iterator() {
    return getAllStashlets().listIterator();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    iterator().forEachRemaining(stashlet -> stashlet.toStringBuilder(builder).append('\n'));
    return "Quaestor{\n" + builder + "}";
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
    // boolean is special
    map.put(Type.type(boolean.class), new BooleanStashlet());
    map.put(Type.type(Boolean.class), map.get(Type.type(boolean.class)));
    return map;
  }

  private static Map<Type, Stashlet> mapServices(ClassLoader loader) {
    Map<Type, Stashlet> map = new LinkedHashMap<>();
    for (Stashlet stashlet : ServiceLoader.load(Stashlet.class, loader)) {
      map.put(stashlet.forType(), stashlet);
    }
    return map;
  }
}
