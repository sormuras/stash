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

import static de.sormuras.stash.compiler.Tag.isMethodChainable;
import static de.sormuras.stash.compiler.Tag.isMethodReturn;
import static de.sormuras.stash.compiler.Tag.isMethodVolatile;
import static de.sormuras.stash.compiler.Tag.isParameterTime;

import de.sormuras.beethoven.Listing;
import de.sormuras.beethoven.unit.Block;
import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.MethodParameter;
import de.sormuras.stash.compiler.Stashlet;

public class StashImplementationMethodBlock extends Block {

  private final StashBuilder builder;
  private final MethodDeclaration method;
  private final String hash;

  StashImplementationMethodBlock(StashBuilder builder, MethodDeclaration method, String hash) {
    this.builder = builder;
    this.method = method;
    this.hash = hash;
  }

  @Override
  public Listing apply(Listing listing) {
    listing.add('{').newline().indent(1);

    boolean record = !isMethodVolatile(method);
    boolean verify = builder.generator.isVerify() && !method.getParameters().isEmpty();
    String buffer = "this." + builder.buffer.getName();
    if (record) {
      listing.eval("{{$}}.putInt({{$}}){{;}}", buffer, hash);
      if (verify) {
        listing.eval("{{$}}.mark(){{;}}", buffer);
      }
      builder
          .generator
          .findTimeParameter(method)
          .ifPresent(
              arg -> {
                listing.add(buffer);
                listing.add(".putLong(");
                listing.add(arg.getName());
                listing.add(" = ");
                listing.add(builder.clock);
                listing.add(".millis());");
                listing.newline();
              });
      for (MethodParameter parameter : method.getParameters()) {
        if (isParameterTime(parameter)) {
          continue;
        }
        Stashlet stashlet = builder.generator.resolve(parameter.getType());
        stashlet.stash(listing, buffer, parameter.getName());
        listing.add(';');
        listing.newline();
      }
      if (verify) {
        listing.eval("{{$}}.reset(){{;}}", buffer);
      }
    }

    applyCallAndReturn(listing, buffer);

    listing.indent(-1).add('}').newline();
    return listing;
  }

  private void applyCallAndReturn(Listing listing, String buffer) {
    boolean returns = isMethodReturn(method);
    if (isMethodVolatile(method)) {
      if (returns) {
        listing.add("return ");
      }
      builder.generator.applyCall(listing, method);
      return;
    }
    String result = "$$result";
    if (returns) {
      listing.eval("{{L}} {{$}} = ", method.getReturnType(), result);
    }
    if (builder.generator.isVerify()) {
      listing.eval("{{$}}(){{;}}", builder.generator.buildSpawnMethodName(method, hash));
    } else {
      builder.generator.applyCall(listing, method);
    }

    // "commit"
    listing.eval("{{$}}.putLong(0, ++this.{{$}}){{;}}", buffer, builder.counter.getName());

    if (returns) {
      listing.add("return ").add(result);
      if (isMethodChainable(method)) {
        listing.eval(" == {{$}} ? this : {{$}}", builder.generator.buildOtherName(), result);
      }
      listing.add(';');
      listing.newline();
    }
  }
}
