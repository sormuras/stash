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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

  /** Built-in byte buffer data input/output routines. */
  interface Buffer {

    /**
     * Store arbitrary serializable object.
     *
     * @param target buffer to write to
     * @param object to write
     */
    static ByteBuffer stashAny(ByteBuffer target, Object object) {
      try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
          ObjectOutputStream stream = new ObjectOutputStream(bytes)) {
        stream.writeObject(object);
        return stashByteArray(target, bytes.toByteArray());
      } catch (Exception e) {
        throw new RuntimeException("Writing object " + object + " failed!", e);
      }
    }

    /**
     * Read arbitrary serializable object.
     *
     * @param source byte buffer to read from
     * @return object read from source buffer
     */
    static Object spawnAny(ByteBuffer source) {
      byte[] byteArray = spawnByteArray(source);
      try (ByteArrayInputStream bytes = new ByteArrayInputStream(byteArray);
          ObjectInputStream stream = new ObjectInputStream(bytes)) {
        return stream.readObject();
      } catch (Exception e) {
        throw new RuntimeException("Reading object from " + source + " failed!", e);
      }
    }

    /**
     * Write a boolean value to the target buffer.
     *
     * @param target buffer to write to
     * @param value the boolean value to be written
     */
    static ByteBuffer stashBoolean(ByteBuffer target, boolean value) {
      return target.put(value ? (byte) 1 : 0);
    }

    /**
     * Read a boolean value from the source buffer.
     *
     * @param source byte buffer to read from
     * @return boolean value
     */
    static boolean spawnBoolean(ByteBuffer source) {
      return source.get() == 1;
    }

    /**
     * Write a byte array to the target buffer.
     *
     * @param target buffer to write to
     * @param value the byte array to be written
     */
    static ByteBuffer stashByteArray(ByteBuffer target, byte[] value) {
      stashIntN(target, value.length);
      return target.put(value);
    }

    /**
     * Read a byte array from the source buffer.
     *
     * @param source byte buffer to read from
     * @return byte array
     */
    static byte[] spawnByteArray(ByteBuffer source) {
      byte[] value = new byte[spawnIntN(source)];
      source.get(value);
      return value;
    }

    /**
     * Write an enum value by its ordinal, its position in its enum declaration.
     *
     * @param target buffer to write to
     * @param value value to write, must not be null
     */
    static <E extends Enum<E>> ByteBuffer stashEnum(ByteBuffer target, E value) {
      return stashIntN(target, value.ordinal());
    }

    /**
     * Get next enum value from source buffer.
     *
     * @param source byte buffer to read from
     * @param values values to select one from
     * @return the enum value
     */
    static <E extends Enum<E>> E spawnEnum(ByteBuffer source, E[] values) {
      return values[spawnIntN(source)];
    }

    /**
     * Write a positive (and small) int value.
     *
     * @param target buffer to write to
     * @param value int &gt;= 0 to be written
     */
    static ByteBuffer stashIntN(ByteBuffer target, @N int value) {
      if (value < 0)
        throw new IllegalArgumentException("@N int value must be positive, but it is " + value);
      while (value > Byte.MAX_VALUE) {
        target.put((byte) (0x80 | (value & 0x7F)));
        value >>>= 7;
      }
      return target.put((byte) value);
    }

    /**
     * Read next positive int value.
     *
     * @param source byte buffer to read from
     * @return int value &gt;= 0
     */
    static @N int spawnIntN(ByteBuffer source) {
      int b = source.get();
      if (b >= 0) return b;
      int result = b & 0x7F;
      int shift = 1;
      while (true) {
        b = source.get();
        result |= (b & 0x7F) << (7 * shift);
        if (b >= 0) return result;
        shift++;
      }
    }

    /**
     * Store a positive long value.
     *
     * @param target buffer to write to
     * @param value long &gt;= 0 to be written
     */
    static ByteBuffer stashLongN(ByteBuffer target, @N long value) {
      if (value < 0)
        throw new IllegalArgumentException("@N long value must be positive, but it is " + value);
      while (value > Byte.MAX_VALUE) {
        target.put((byte) (0x80 | (value & 0x7F)));
        value >>>= 7;
      }
      return target.put((byte) value);
    }

    /**
     * Read next positive long value.
     *
     * @param source byte buffer to read from
     * @return long value &gt;= 0
     */
    static @N long spawnLongN(ByteBuffer source) {
      long b = source.get();
      if (b >= 0) return b;
      long result = b & 0x7F;
      int shift = 1;
      while (true) {
        b = source.get();
        result |= (b & 0x7F) << (7 * shift);
        if (b >= 0) return result;
        shift++;
      }
    }

    /**
     * Store string using UTF 8 charset.
     *
     * @param target buffer to write to
     * @param string string to store
     */
    static ByteBuffer stashString(ByteBuffer target, String string) {
      return stashString(target, string, StandardCharsets.UTF_8);
    }

    /**
     * Get string from source buffer using UTF-8 charset.
     *
     * @param source byte buffer to read from
     * @return the string
     * @see #spawnString(ByteBuffer, Charset)
     * @see #stashString(ByteBuffer, String)
     */
    static String spawnString(ByteBuffer source) {
      return spawnString(source, StandardCharsets.UTF_8);
    }

    /**
     * Store string using UTF 8 charset.
     *
     * @param target buffer to write to
     * @param string string to store
     * @param charset charset used for encoding
     */
    static ByteBuffer stashString(ByteBuffer target, String string, Charset charset) {
      byte[] bytes = string.getBytes(charset);
      stashIntN(target, bytes.length);
      return target.put(bytes);
    }

    /**
     * Get string from source buffer using specified charset.
     *
     * @param source byte buffer to read from
     * @param charset the charset used by interpreting the byte array
     * @return the string
     * @see #spawnString(ByteBuffer)
     * @see #stashString(ByteBuffer, String, Charset)
     * @see String#String(byte[], Charset)
     */
    static String spawnString(ByteBuffer source, Charset charset) {
      byte[] bytes = new byte[spawnIntN(source)];
      source.get(bytes);
      return new String(bytes, charset);
    }

    interface View {

      static ByteBuffer stashCharArray(ByteBuffer target, char[] value) {
        stashIntN(target, value.length);
        target.asCharBuffer().put(value);
        target.position(target.position() + value.length * Character.BYTES);
        return target;
      }

      static char[] spawnCharArray(ByteBuffer source) {
        char[] array = new char[spawnIntN(source)];
        source.asCharBuffer().get(array);
        source.position(source.position() + array.length * Character.BYTES);
        return array;
      }

      static ByteBuffer stashDoubleArray(ByteBuffer target, double[] value) {
        stashIntN(target, value.length);
        target.asDoubleBuffer().put(value);
        target.position(target.position() + value.length * Double.BYTES);
        return target;
      }

      static double[] spawnDoubleArray(ByteBuffer source) {
        double[] array = new double[spawnIntN(source)];
        source.asDoubleBuffer().get(array);
        source.position(source.position() + array.length * Double.BYTES);
        return array;
      }

      static ByteBuffer stashFloatArray(ByteBuffer target, float[] value) {
        stashIntN(target, value.length);
        target.asFloatBuffer().put(value);
        target.position(target.position() + value.length * Float.BYTES);
        return target;
      }

      static float[] spawnFloatArray(ByteBuffer source) {
        float[] array = new float[spawnIntN(source)];
        source.asFloatBuffer().get(array);
        source.position(source.position() + array.length * Float.BYTES);
        return array;
      }

      static ByteBuffer stashIntArray(ByteBuffer target, int[] value) {
        stashIntN(target, value.length);
        target.asIntBuffer().put(value);
        target.position(target.position() + value.length * Integer.BYTES);
        return target;
      }

      static int[] spawnIntArray(ByteBuffer source) {
        int[] array = new int[spawnIntN(source)];
        source.asIntBuffer().get(array);
        source.position(source.position() + array.length * Integer.BYTES);
        return array;
      }

      static ByteBuffer stashLongArray(ByteBuffer target, long[] value) {
        stashIntN(target, value.length);
        target.asLongBuffer().put(value);
        target.position(target.position() + value.length * Long.BYTES);
        return target;
      }

      static long[] spawnLongArray(ByteBuffer source) {
        long[] array = new long[spawnIntN(source)];
        source.asLongBuffer().get(array);
        source.position(source.position() + array.length * Long.BYTES);
        return array;
      }

      static ByteBuffer stashShortArray(ByteBuffer target, short[] value) {
        stashIntN(target, value.length);
        target.asShortBuffer().put(value);
        target.position(target.position() + value.length * Short.BYTES);
        return target;
      }

      static short[] spawnShortArray(ByteBuffer source) {
        short[] array = new short[spawnIntN(source)];
        source.asShortBuffer().get(array);
        source.position(source.position() + array.length * Short.BYTES);
        return array;
      }
    }
  }
}
