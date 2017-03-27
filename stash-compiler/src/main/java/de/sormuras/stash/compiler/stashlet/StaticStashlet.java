package de.sormuras.stash.compiler.stashlet;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.Name;
import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.compiler.Stashlet;

public class StaticStashlet implements Stashlet {

  private final Type type;
  private final Name stashName;
  private final Name spawnName;

  public StaticStashlet(Type type, Name stashName, Name spawnName) {
    this.type = type;
    this.stashName = stashName;
    this.spawnName = spawnName;
  }

  @Override
  public Type forType() {
    return type;
  }

  @Override
  public Listing stash(Listing listing, String buffer, String parameterName) {
    return listing.add(stashName).add('(').add(buffer).add(", ").add(parameterName).add(')');
  }

  @Override
  public Listing spawn(Listing listing, String buffer, Type parameterType) {
    return listing.add(spawnName).add('(').add(buffer).add(')');
  }
}
