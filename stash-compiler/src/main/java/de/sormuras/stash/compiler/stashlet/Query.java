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

package de.sormuras.stash.compiler.stashlet;

import de.sormuras.beethoven.type.Type;
import de.sormuras.stash.Stashable;
import java.lang.annotation.Annotation;

public class Query {

  @SafeVarargs
  static Query key(Class<?> classType, Class<? extends Annotation>... annotations) {
    boolean isEnum = classType.isEnum();
    boolean isStashable = Stashable.isStashable(classType);
    Type type = Type.type(classType);
    if (annotations.length > 0) {
      type = Type.withAnnotations(type, annotations);
    }
    return new Query(type, isStashable, isEnum);
  }

  private final Type type;
  private final boolean isStashable;
  private final boolean isEnum;

  public Query(Type type, boolean isStashable, boolean isEnum) {
    this.type = type;
    this.isStashable = isStashable;
    this.isEnum = isEnum;
  }

  public Type getType() {
    return type;
  }

  public boolean isEnum() {
    return isEnum;
  }

  public boolean isStashable() {
    return isStashable;
  }
}
