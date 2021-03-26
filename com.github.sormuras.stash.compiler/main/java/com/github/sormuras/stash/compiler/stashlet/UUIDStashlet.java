package com.github.sormuras.stash.compiler.stashlet;

import com.github.sormuras.beethoven.Listing;
import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.stash.compiler.Stashlet;
import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDStashlet implements Stashlet {

  public static ByteBuffer stash(ByteBuffer target, UUID uuid) {
    return target.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
  }

  public static UUID spawn(ByteBuffer source) {
    return new UUID(source.getLong(), source.getLong());
  }

  public UUIDStashlet() {}

  @Override
  public Type forType() {
    return Type.type(UUID.class);
  }

  @Override
  public Listing stash(Listing listing, String buffer, String parameterName) {
    listing.add(buffer);
    listing.add(".putLong(").add(parameterName).add(".getMostSignificantBits())");
    listing.add(".putLong(").add(parameterName).add(".getLeastSignificantBits())");
    return listing;
  }

  @Override
  public Listing spawn(Listing listing, String buffer, Type parameterType) {
    listing.add("new ");
    listing.add(parameterType);
    listing.add('(');
    listing.add(buffer);
    listing.add(".getLong(), ");
    listing.add(buffer);
    listing.add(".getLong())");
    return listing;
  }
}
