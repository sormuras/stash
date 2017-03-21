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

package demo;

import de.sormuras.beethoven.Listable;
import de.sormuras.beethoven.Name;
import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.Stashable.Buffer;
import de.sormuras.stash.compiler.Stashlet;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BigIntegerStashlet implements Stashlet<BigInteger> {

  private static final Name STASH = Name.reflect(Buffer.class, "stashByteArray");
  private static final Name SPAWN = Name.reflect(Buffer.class, "spawnByteArray");

  @Override
  public Type forType() {
    return Type.type(BigInteger.class);
  }

  @Override
  public ByteBuffer stash(ByteBuffer target, BigInteger value) {
    return Buffer.stashByteArray(target, value.toByteArray());
  }

  @Override
  public BigInteger spawn(ByteBuffer source, Class<? extends BigInteger> type) {
    return new BigInteger(Buffer.spawnByteArray(source));
  }

  @Override
  public Listable stash(String target, String name) {
    return listing -> listing.add(STASH).eval("({{$}}, {{$}}.toByteArray())", target, name);
  }

  @Override
  public Listable spawn(String source, Type type) {
    return listing -> listing.eval("new {{N}}({{N}}({{$}}))", BigInteger.class, SPAWN, source);
  }
}
