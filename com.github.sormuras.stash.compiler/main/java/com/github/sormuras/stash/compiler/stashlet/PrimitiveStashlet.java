package com.github.sormuras.stash.compiler.stashlet;

import static com.github.sormuras.beethoven.type.PrimitiveType.primitive;

import com.github.sormuras.beethoven.Listing;
import com.github.sormuras.beethoven.Name;
import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.stash.compiler.Stashlet;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/** Call site: instance of java.nio.ByteBuffer. */
public class PrimitiveStashlet<T> implements Stashlet {

  @SuppressWarnings("unchecked")
  public static <T> PrimitiveStashlet<T> of(Class<T> type) {
    String suffix = type.getName();
    suffix = type == byte.class ? "" : suffix.substring(0, 1).toUpperCase() + suffix.substring(1);
    try {
      Method stashMethod = ByteBuffer.class.getMethod("put" + suffix, type);
      Method spawnMethod = ByteBuffer.class.getMethod("get" + suffix);
      return (PrimitiveStashlet<T>)
          new PrimitiveStashlet<>(primitive(type), stashMethod, spawnMethod);
    } catch (NoSuchMethodException exception) {
      throw new AssertionError("Accessor for " + type + " not found!", exception);
    }
  }

  private final Type type;
  private final Name stashName;
  private final Name spawnName;
  private final StashFunction<T> stashFunction;
  private final SpawnFunction<T> spawnFunction;

  private PrimitiveStashlet(Type type, Method stashMethod, Method spawnMethod) {
    this.type = type;
    this.stashName = Name.name(stashMethod);
    this.spawnName = Name.name(spawnMethod);
    this.stashFunction = StashFunction.of(stashMethod);
    this.spawnFunction = SpawnFunction.of(spawnMethod);
  }

  @Override
  public Type forType() {
    return type;
  }

  @Override
  public Listing stash(Listing listing, String buffer, String parameterName) {
    listing.add(buffer);
    listing.add('.');
    listing.add(stashName.lastName());
    listing.add('(');
    listing.add(parameterName);
    listing.add(')');
    return listing;
  }

  @Override
  public Listing spawn(Listing listing, String buffer, Type parameterType) {
    listing.add(buffer);
    listing.add('.');
    listing.add(spawnName.lastName());
    listing.add("()");
    return listing;
  }
}
