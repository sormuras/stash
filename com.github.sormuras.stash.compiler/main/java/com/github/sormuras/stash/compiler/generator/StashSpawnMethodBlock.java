package com.github.sormuras.stash.compiler.generator;

import static com.github.sormuras.stash.compiler.Tag.isMethodReturn;
import static com.github.sormuras.stash.compiler.Tag.isParameterTime;

import com.github.sormuras.beethoven.Listing;
import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.beethoven.unit.Block;
import com.github.sormuras.beethoven.unit.MethodDeclaration;
import com.github.sormuras.beethoven.unit.MethodParameter;
import com.github.sormuras.stash.compiler.Stashlet;

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

    String buffer = "this." + builder.buffer.getName();
    builder
        .generator
        .findTimeParameter(method)
        .ifPresent(arg -> listing.eval("long {{$}} = {{$}}.getLong(){{;}}", arg.getName(), buffer));

    for (MethodParameter parameter : method.getParameters()) {
      if (isParameterTime(parameter)) {
        continue;
      }
      Type type = parameter.getType();
      listing.add(type);
      listing.add(' ');
      listing.add(parameter.getName());
      listing.add(' ');
      listing.add('=');
      listing.add(' ');
      Stashlet stashlet = builder.generator.resolve(type);
      stashlet.spawn(listing, buffer, type);
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
