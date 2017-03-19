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

package de.sormuras.stash;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Prevalent System Interface annotation. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Stash {

  /**
   * Stash version.
   *
   * <p>Same as noted in {@code gradle.properties}.
   */
  String VERSION = "1.0-SNAPSHOT";

  /** @return super class of the generated stash class. */
  Class<?> classExtends() default Object.class;

  /** @return comments to be copied into the {@link javax.annotation.Generated} annotation. */
  String comments() default "";

  /** @return {@code true} to generate stash/spawn verification code, {@code false} to omit it. */
  boolean verify() default true;
}
