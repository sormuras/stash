package de.sormuras.stash.compiler;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.type.Type;
import de.sormuras.beethoven.unit.InterfaceDeclaration;

public interface Stashlet {

  default void init(Generator generator, InterfaceDeclaration io) {
    // empty
  }

  Listing stash(Listing listing, String buffer, String parameterName);

  Listing spawn(Listing listing, String buffer, Type parameterType);
}
