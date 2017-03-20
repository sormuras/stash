/*
 * Copyright (C) 2017 Christian Stein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.sormuras.stash.compiler.generator;

import static de.sormuras.stash.compiler.Tag.isMethodVolatile;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.Name;
import de.sormuras.beethoven.unit.Block;
import de.sormuras.beethoven.unit.MethodDeclaration;
import java.nio.ByteBuffer;
import java.time.Clock;
import javax.lang.model.element.Modifier;

class StashConstructor extends MethodDeclaration {

  private final StashBuilder builder;
  private final String buffer;
  private final String counter;
  private final String clock;

  StashConstructor(StashBuilder builder) {
    this.builder = builder;
    this.buffer = builder.buffer.getName();
    this.counter = builder.counter.getName();
    this.clock = builder.clock.getName();

    setModifiers(Modifier.PUBLIC);
    setName("<init>");
    declareParameter(builder.other.getType(), builder.other.getName());
    declareParameter(ByteBuffer.class, buffer);
    setBody(new Body());
  }

  class Body extends Block {

    private void assign(Listing listing, String name) {
      assign(listing, name, name);
    }

    private void assign(Listing listing, String left, String right) {
      listing.add("this.").add(left).add(" = ").add(right).add(';').newline();
    }

    private void assign(Listing listing, String left, Name right, String args) {
      listing.add("this.").add(left).add(" = ").add(right).add(args).add(';').newline();
    }

    @Override
    public Listing apply(Listing listing) {
      listing.add('{').newline().indent(1);

      assign(listing, builder.other.getName());
      assign(listing, buffer);
      assign(listing, counter, buffer + ".getLong()");
      assign(listing, clock, Name.reflect(Clock.class, "systemUTC"), "()");

      listing.add("for (long index = 0; index < counter; index++) {").newline().indent(1);
      listing.add("int hash = ").add(buffer).add(".getInt();").newline();
      listing.add("switch (hash) {").newline().indent(1);
      for (MethodDeclaration method : builder.generator.getInterfaceDeclaration().getMethods()) {
        if (isMethodVolatile(method)) {
          continue;
        }
        String hash = builder.generator.buildMethodHash(method);
        String call = builder.generator.buildSpawnMethodName(method, hash);
        listing.add("case ").add(hash).add(": ").add(call).add("(); break;").newline();
      }
      listing.add("default: throw new AssertionError(index);").newline();
      listing.indent(-1).add("}").newline(); // switch
      listing.indent(-1).add("}").newline(); // for
      listing.add(buffer).add(".limit(").add(buffer).add(".capacity());").newline();

      listing.indent(-1).add('}').newline();
      return listing;
    }
  }
}
