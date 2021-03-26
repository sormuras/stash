package com.github.sormuras.stash.compiler.stashlet;

import com.github.sormuras.beethoven.Listing;
import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.stash.Stashable;
import com.github.sormuras.stash.compiler.Stashlet;
import java.nio.ByteBuffer;

public class StashableStashlet implements Stashlet {

  public StashableStashlet() {}

  @Override
  public Type forType() {
    return Type.type(Stashable.class);
  }

  public <S extends Stashable> ByteBuffer stash(ByteBuffer target, S stashable) {
    return stashable.stash(target);
  }

  public Stashable spawn(ByteBuffer source, Class<? extends Stashable> type) throws Exception {
    return type.getConstructor(ByteBuffer.class).newInstance(source);
  }

  @Override
  public Listing stash(Listing listing, String buffer, String parameterName) {
    listing.add(parameterName);
    listing.add(".stash");
    listing.add('(');
    listing.add(buffer);
    listing.add(')');
    return listing;
  }

  @Override
  public Listing spawn(Listing listing, String buffer, Type parameterType) {
    listing.add("new ");
    listing.add(parameterType);
    listing.add('(');
    listing.add(buffer);
    listing.add(')');
    return listing;
  }
}
