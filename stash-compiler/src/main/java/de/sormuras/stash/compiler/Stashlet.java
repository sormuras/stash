package de.sormuras.stash.compiler;

import static java.lang.invoke.MethodHandleProxies.asInterfaceInstance;
import static java.lang.invoke.MethodHandles.publicLookup;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.type.Type;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stashlet {

  default Type forType() {
    return Type.type(Object.class);
  }

  default void init(Type type, Generator generator) {
    // empty
  }

  Listing stash(Listing listing, String buffer, String parameterName);

  Listing spawn(Listing listing, String buffer, Type parameterType);

  @FunctionalInterface
  interface StashFunction<T> extends BiFunction<ByteBuffer, T, ByteBuffer> {

    static boolean matches(Method method) {
      if (!method.getReturnType().equals(ByteBuffer.class)) {
        return false;
      }
      if (method.getParameterCount() != 2) {
        return false;
      }
      return method.getParameterTypes()[0].equals(ByteBuffer.class);
    }

    @SuppressWarnings("unchecked")
    static <T> StashFunction<T> of(Method method) {
      try {
        return asInterfaceInstance(StashFunction.class, publicLookup().unreflect(method));
      } catch (ReflectiveOperationException exception) {
        throw new AssertionError("Creating StashFunction instance failed!", exception);
      }
    }
  }

  @FunctionalInterface
  interface SpawnFunction<T> extends Function<ByteBuffer, T> {

    static boolean matches(Method method) {
      if (method.getReturnType().equals(void.class)) {
        return false;
      }
      if (method.getParameterCount() != 1) {
        return false;
      }
      return method.getParameterTypes()[0].equals(ByteBuffer.class);
    }

    @SuppressWarnings("unchecked")
    static <T> SpawnFunction<T> of(Method method) {
      try {
        return asInterfaceInstance(SpawnFunction.class, publicLookup().unreflect(method));
      } catch (ReflectiveOperationException exception) {
        throw new AssertionError("Creating SpawnFunction instance failed!", exception);
      }
    }
  }
}
