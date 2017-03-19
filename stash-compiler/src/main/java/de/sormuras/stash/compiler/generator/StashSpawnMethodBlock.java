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
import de.sormuras.beethoven.unit.MethodParameter;
import de.sormuras.stash.compiler.Generator;
import de.sormuras.stash.compiler.Stashlet;
import java.util.Optional;

public class StashSpawnMethodBlock extends Block {

  final Generator generator;
  final MethodDeclaration method;

  StashSpawnMethodBlock(Generator generator, MethodDeclaration method) {
    this.generator = generator;
    this.method = method;
  }

  @Override
  public Listing apply(Listing listing) {
    listing.add('{').newline().indent(1);

    String source = "buffer"; // TODO generator.getSpawnByteBufferName();
    Optional<MethodParameter> time = generator.getTimeParameter(method);
    time.ifPresent(arg -> listing.eval("long {{$}} = {{$}}.getLong(){{;}}", arg.getName(), source));

    for (MethodParameter parameter : method.getParameters()) {
      if (generator.isParameterTime(parameter)) {
        continue;
      }
      listing.add(parameter.getType());
      listing.add(' ');
      listing.add(parameter.getName());
      listing.add(' ');
      listing.add('=');
      listing.add(' ');
      Stashlet<?> stashlet = null; // TODO generator.resolve(parameter);
      //listing.add(stashlet.spawn(source, parameter.getType()));
      listing.add(';');
      listing.newline();
    }
    if (generator.isMethodReturn(method)) {
      listing.add("return ");
    }
    generator.applyCall(listing, method);

    listing.indent(-1).add('}').newline();
    return listing;
  }
}
