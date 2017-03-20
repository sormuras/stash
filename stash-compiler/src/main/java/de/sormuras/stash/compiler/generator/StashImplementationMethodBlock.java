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

    String target = builder.buffer.getName();
    boolean record = !builder.generator.isMethodVolatile(method);
    boolean verify = builder.generator.isVerify() && !method.getParameters().isEmpty();
    if (record) {
      listing.eval("{{$}}.putInt({{$}}){{;}}", target, hash);
      if (verify) {
        listing.eval("{{$}}.mark(){{;}}", target);
      }
      for (MethodParameter parameter : method.getParameters()) {
        if (builder.generator.isParameterTime(parameter)) {
          continue;
        }
        Stashlet<?> stashlet = builder.generator.resolve(parameter);
        listing.add(stashlet.stash(target, parameter.getName()));
        listing.add(';');
        listing.newline();
      }
      if (verify) {
        listing.eval("{{$}}.reset(){{;}}", target);
      }
    }

    // "applyCallAndReturn"
    boolean returns = builder.generator.isMethodReturn(method);
    if (builder.generator.isMethodVolatile(method)) {
      if (returns) {
        listing.add("return ");
      }
      return builder.generator.applyCall(listing, method);
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
    listing.eval("{{$}}.putLong(0, ++{{$}}){{;}}", target, builder.counter.getName());

    if (returns) {
      listing.add("return ").add(result);
      if (builder.generator.isMethodChainable(method)) {
        listing.eval(" == {{$}} ? this : {{$}}", builder.generator.buildOtherName(), result);
      }
      listing.add(';');
      listing.newline();
    }
    //

    listing.indent(-1).add('}').newline();
    return listing;
  }
}
