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

import static java.lang.String.format;

import java.nio.ByteBuffer;

/** Implementation indicates that it knows how to stash to and spawn from a byte buffer. */
public interface Stashable {

  /**
   * Validate a class for proper Stashable implementation.
   *
   * @param candidate class to validate
   * @throws IllegalArgumentException if candidate doesn't meet all requirements
   */
  static void check(Class<?> candidate) throws IllegalArgumentException {
    if (candidate.isInterface() || !Stashable.class.isAssignableFrom(candidate)) {
      String message = format("%s doesn't implement %s", candidate, Stashable.class);
      throw new IllegalArgumentException(message);
    }
    try {
      candidate.getDeclaredConstructor(ByteBuffer.class);
    } catch (NoSuchMethodException e) {
      String message = "%s doesn't provide constructor with %s as single parameter";
      throw new IllegalArgumentException(format(message, candidate, ByteBuffer.class), e);
    }
  }

  static boolean isStashable(Class<?> classType) {
    try {
      Stashable.check(classType);
      return true;
    } catch (NullPointerException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Write all data needed for spawning this object.
   *
   * @param target the buffer to write to
   * @return same target buffer passed as the single parameter
   */
  ByteBuffer stash(ByteBuffer target);
}
