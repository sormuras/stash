package de.sormuras.stash.compiler.stashlet;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.compiler.Stashlet;
import java.nio.ByteBuffer;

public class BooleanStashlet implements Stashlet {

  public static ByteBuffer stashBoolean(ByteBuffer target, boolean value) {
    return target.put((byte) (value ? 1 : 0));
  }

  public static boolean spawnBoolean(ByteBuffer source) {
    return source.get() == 1;
  }

  @Override
  public Listing stash(Listing listing, String buffer, String parameterName) {
    return listing.add(buffer).add(".put((byte) (").add(parameterName).add(" ? 1 : 0))");
  }

  @Override
  public Listing spawn(Listing listing, String buffer, Type parameterType) {
    return listing.add(buffer).add(".get() == 1");
  }
}
