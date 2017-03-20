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

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.unit.Block;
import de.sormuras.beethoven.unit.MethodDeclaration;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.List;
import javax.lang.model.element.Modifier;

class StashConstructor extends MethodDeclaration {

  private final StashBuilder builder;
  private final String buffer;

  StashConstructor(StashBuilder builder) {
    this.builder = builder;
    this.buffer = builder.buffer.getName();

    setModifiers(Modifier.PUBLIC);
    setName("<init>");
    declareParameter(builder.other.getType(), builder.other.getName());
    declareParameter(ByteBuffer.class, buffer);
    setBody(new Body());
  }

  class Body extends Block {

    private void assign(Listing listing, String name) {
      listing.add("this.").add(name).add(" = ").add(name).add(';').newline();
    }

    @Override
    public Listing apply(Listing listing) {
      listing.add('{').newline().indent(1);

      assign(listing, builder.other.getName());
      assign(listing, buffer);

      //for (long index = 0; index < buffer.getLong(); index++) {
      //  int hash = buffer.getInt();
      //  switch (hash) {
      //    case 0x1345890F: generateRespawn();
      //    ...
      //    default: throw new AssertionError(index);
      //  }
      //}
      //buffer.limit(buffer.capacity());

      List<MethodDeclaration> methods = builder.generator.getInterfaceDeclaration().getMethods();

      listing
          .add("for (long index = 0; index < ")
          .add(buffer)
          .add(".getLong(); index++) {")
          .newline()
          .indent(1);
      listing.add("int hash = ").add(buffer).add(".getInt();").newline();
      listing.add("switch (hash) {").newline().indent(1);
      for (MethodDeclaration method : methods) {
        String hash = builder.generator.buildMethodHash(method);
        String call = builder.generator.buildSpawnMethodName(method, hash);
        listing.add("case ").add(hash).add(": ").add(call).add("(); break;").newline();
      }
      listing.indent(-1).add("}").newline(); // switch
      listing.indent(-1).add("}").newline(); // for
      listing.add(buffer).add(".limit(").add(buffer).add(".capacity());").newline();

      listing.indent(-1).add('}').newline();
      return listing;
    }
  }
}
