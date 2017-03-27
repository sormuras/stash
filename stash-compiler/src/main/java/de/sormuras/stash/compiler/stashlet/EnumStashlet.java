package de.sormuras.stash.compiler.stashlet;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.compiler.Stashlet;
import java.nio.ByteBuffer;

public class EnumStashlet implements Stashlet {

  public static <E extends Enum<E>> ByteBuffer stashEnum(ByteBuffer target, E constant) {
    return target.put((byte) constant.ordinal());
  }

  public static <E extends Enum<E>> E spawnEnum(ByteBuffer source, Class<E> enumClass) {
    return enumClass.getEnumConstants()[Byte.toUnsignedInt(source.get())];
  }

  @Override
  public Listing stash(Listing listing, String buffer, String parameterName) {
    return listing.add(buffer).add(".put((byte) ").add(parameterName).add(".ordinal())");
  }

  @Override
  public Listing spawn(Listing listing, String buffer, Type parameterType) {
    listing.add(parameterType);
    listing.add(".values()[");
    listing.add(Type.type(Byte.class));
    listing.add(".toUnsignedInt(");
    listing.add(buffer);
    listing.add(".get())]");
    return listing;
  }
}
