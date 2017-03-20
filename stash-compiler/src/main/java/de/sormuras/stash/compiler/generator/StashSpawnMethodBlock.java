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

import static de.sormuras.stash.compiler.Tag.isMethodReturn;
import static de.sormuras.stash.compiler.Tag.isParameterTime;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.unit.Block;
import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.MethodParameter;
import de.sormuras.stash.compiler.Stashlet;

public class StashSpawnMethodBlock extends Block {

  private final StashBuilder builder;
  private final MethodDeclaration method;

  StashSpawnMethodBlock(StashBuilder builder, MethodDeclaration method) {
    this.builder = builder;
    this.method = method;
  }

  @Override
  public Listing apply(Listing listing) {
    listing.add('{').newline().indent(1);

    String source = builder.buffer.getName();
    builder
        .generator
        .findTimeParameter(method)
        .ifPresent(arg -> listing.eval("long {{$}} = {{$}}.getLong(){{;}}", arg.getName(), source));

    for (MethodParameter parameter : method.getParameters()) {
      if (isParameterTime(parameter)) {
        continue;
      }
      listing.add(parameter.getType());
      listing.add(' ');
      listing.add(parameter.getName());
      listing.add(' ');
      listing.add('=');
      listing.add(' ');
      Stashlet<?> stashlet = builder.generator.resolve(parameter);
      listing.add(stashlet.spawn(source, parameter.getType()));
      listing.add(';');
      listing.newline();
    }
    if (isMethodReturn(method)) {
      listing.add("return ");
    }
    builder.generator.applyCall(listing, method);

    listing.indent(-1).add('}').newline();
    return listing;
  }
}
