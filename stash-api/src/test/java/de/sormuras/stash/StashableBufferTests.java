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

import static de.sormuras.stash.Stashable.Buffer.View.spawnCharArray;
import static de.sormuras.stash.Stashable.Buffer.View.spawnDoubleArray;
import static de.sormuras.stash.Stashable.Buffer.View.spawnFloatArray;
import static de.sormuras.stash.Stashable.Buffer.View.spawnIntArray;
import static de.sormuras.stash.Stashable.Buffer.View.spawnLongArray;
import static de.sormuras.stash.Stashable.Buffer.View.spawnShortArray;
import static de.sormuras.stash.Stashable.Buffer.View.stashCharArray;
import static de.sormuras.stash.Stashable.Buffer.View.stashDoubleArray;
import static de.sormuras.stash.Stashable.Buffer.View.stashFloatArray;
import static de.sormuras.stash.Stashable.Buffer.View.stashIntArray;
import static de.sormuras.stash.Stashable.Buffer.View.stashLongArray;
import static de.sormuras.stash.Stashable.Buffer.View.stashShortArray;
import static de.sormuras.stash.Stashable.Buffer.spawnAny;
import static de.sormuras.stash.Stashable.Buffer.spawnBoolean;
import static de.sormuras.stash.Stashable.Buffer.spawnEnum;
import static de.sormuras.stash.Stashable.Buffer.spawnIntN;
import static de.sormuras.stash.Stashable.Buffer.spawnLongN;
import static de.sormuras.stash.Stashable.Buffer.spawnString;
import static de.sormuras.stash.Stashable.Buffer.stashAny;
import static de.sormuras.stash.Stashable.Buffer.stashBoolean;
import static de.sormuras.stash.Stashable.Buffer.stashByteArray;
import static de.sormuras.stash.Stashable.Buffer.stashEnum;
import static de.sormuras.stash.Stashable.Buffer.stashIntN;
import static de.sormuras.stash.Stashable.Buffer.stashLongN;
import static de.sormuras.stash.Stashable.Buffer.stashString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.EOFException;
import java.io.NotSerializableException;
import java.io.ObjectStreamConstants;
import java.io.StreamCorruptedException;
import java.math.BigDecimal;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StashableBufferTests {

  private ByteBuffer buffer = ByteBuffer.allocate(1000);

  @BeforeEach
  void before() {
    buffer.clear();
  }

  @Test
  void testStashAny() {
    stashAny(buffer, false);
    stashAny(buffer, BigDecimal.valueOf(Math.PI));
    stashAny(buffer, true);
    buffer.flip();
    assertFalse((boolean) spawnAny(buffer));
    assertEquals(Math.PI, ((Number) spawnAny(buffer)).doubleValue());
    assertTrue((boolean) spawnAny(buffer));
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  @Test
  void testStashAnyFails() {
    RuntimeException e;
    e = assertThrows(RuntimeException.class, () -> stashAny(buffer, new Thread()));
    assertEquals(NotSerializableException.class, e.getCause().getClass());
    // spawn from corrupt stream
    buffer.clear();
    stashByteArray(buffer, new byte[] {1, 4, 5, 6, 7, 8});
    buffer.flip();
    e = assertThrows(RuntimeException.class, () -> spawnAny(buffer));
    assertEquals(StreamCorruptedException.class, e.getCause().getClass());
    // spawn from truncated stream
    buffer.clear();
    stashAny(buffer, "abc");
    buffer.flip();
    buffer.limit(buffer.limit() - 3);
    e = assertThrows(RuntimeException.class, () -> spawnAny(buffer));
    assertEquals(BufferUnderflowException.class, e.getClass());
    // spawn from modified stream 1
    buffer.clear();
    stashAny(buffer, "abc");
    buffer.flip();
    buffer.put(6, (byte) 2);
    e = assertThrows(RuntimeException.class, () -> spawnAny(buffer));
    assertEquals(EOFException.class, e.getCause().getClass());
    // spawn from modified stream 2
    buffer.clear();
    stashAny(buffer, "abc");
    buffer.flip();
    buffer.put(5, ObjectStreamConstants.TC_OBJECT);
    e = assertThrows(RuntimeException.class, () -> spawnAny(buffer));
    assertEquals(StreamCorruptedException.class, e.getCause().getClass());
  }

  @Test
  void testStashBoolean() {
    stashBoolean(buffer, false);
    stashBoolean(buffer, true);
    buffer.flip();
    assertFalse(spawnBoolean(buffer));
    assertTrue(spawnBoolean(buffer));
  }

  @Test
  void testStashEnum() {
    stashEnum(buffer, TimeUnit.DAYS);
    stashEnum(buffer, TimeUnit.MICROSECONDS);
    buffer.flip();
    assertSame(TimeUnit.DAYS, spawnEnum(buffer, TimeUnit.values()));
    assertSame(TimeUnit.MICROSECONDS, spawnEnum(buffer, TimeUnit.values()));
  }

  @Test
  void testStashInt() {
    assertEquals(1, testStashInt(0));
    assertEquals(1, testStashInt(1));
    assertEquals(1, testStashInt(2));
    assertEquals(1, testStashInt(3));
    assertEquals(1, testStashInt(127));
    assertEquals(2, testStashInt(128));
    assertEquals(2, testStashInt(16383));
    assertEquals(3, testStashInt(16384));
    assertEquals(3, testStashInt(2097151));
    assertEquals(4, testStashInt(2097152));
    assertEquals(4, testStashInt(268435455));
    assertEquals(5, testStashInt(268435456));
    assertEquals(5, testStashInt(Integer.MAX_VALUE));
    assertThrows(IllegalArgumentException.class, () -> stashIntN(buffer, -1));
  }

  private int testStashInt(int probe) {
    buffer.clear();
    stashIntN(buffer, probe);
    buffer.flip();
    assertEquals(probe, spawnIntN(buffer));
    return buffer.position();
  }

  @Test
  void testStashLong() {
    assertEquals(1, testStashLong(0));
    assertEquals(1, testStashLong(1));
    assertEquals(1, testStashLong(2));
    assertEquals(1, testStashLong(3));
    assertEquals(1, testStashLong(127));
    assertEquals(2, testStashLong(128));
    assertEquals(2, testStashLong(16383));
    assertEquals(3, testStashLong(16384));
    assertEquals(3, testStashLong(2097151));
    assertEquals(4, testStashLong(2097152));
    assertEquals(4, testStashLong(268435455));
    assertEquals(5, testStashLong(268435456));
    assertEquals(5, testStashLong(Integer.MAX_VALUE));
    assertEquals(5, testStashLong((1L << 31) - 1)); // Integer.MAX_VALUE
    assertEquals(5, testStashLong((1L << 31)));
    assertEquals(5, testStashLong((1L << 31) + 1));
    assertEquals(5, testStashLong((1L << 35) - 1));
    assertEquals(6, testStashLong((1L << 35)));
    assertEquals(6, testStashLong((1L << 42) - 1));
    assertEquals(7, testStashLong((1L << 42)));
    assertEquals(7, testStashLong((1L << 49) - 1));
    assertEquals(8, testStashLong((1L << 49)));
    assertEquals(8, testStashLong((1L << 56) - 1));
    assertEquals(9, testStashLong((1L << 56)));
    assertEquals(9, testStashLong(Long.MAX_VALUE));
    assertThrows(IllegalArgumentException.class, () -> stashLongN(buffer, -1L));
  }

  private int testStashLong(long expected) {
    buffer.clear();
    stashLongN(buffer, expected);
    buffer.flip();
    long actual = spawnLongN(buffer);
    assertEquals(expected, actual);
    return buffer.position();
  }

  @Test
  void testStashNullEnum() {
    assertThrows(NullPointerException.class, () -> stashEnum(buffer, null));
  }

  @Test
  void testStashString() {
    testStashString("");
    testStashString("123");
    testStashString("\tABC.xyz╝¢¥║¬░▒▓│»\u7FFF\u8000");
  }

  private void testStashString(String string) {
    buffer.clear();
    stashString(buffer, string);
    buffer.flip();
    String actual = spawnString(buffer);
    assertEquals(string, actual);
  }

  @Test
  void testStashViewCharArray() {
    char[] expected = {'\0', 'a', 'Ω', Character.MAX_VALUE, Character.MIN_VALUE};
    stashCharArray(buffer, expected);
    buffer.flip();
    char[] actual = spawnCharArray(buffer);
    assertNotSame(expected, actual);
    assertArrayEquals(expected, actual);
  }

  @Test
  void testStashViewDoubleArray() {
    double[] expected = {47, 11, 18, 0, Double.MAX_VALUE, Double.MIN_NORMAL, Double.MIN_VALUE};
    stashDoubleArray(buffer, expected);
    buffer.flip();
    double[] actual = spawnDoubleArray(buffer);
    assertNotSame(expected, actual);
    assertArrayEquals(expected, actual);
  }

  @Test
  void testStashViewFloatArray() {
    float[] expected = {47, 11, 18, 0, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL};
    stashFloatArray(buffer, expected);
    buffer.flip();
    float[] actual = spawnFloatArray(buffer);
    assertNotSame(expected, actual);
    assertArrayEquals(expected, actual);
  }

  @Test
  void testStashViewIntArray() {
    int[] expected = {47, 11, 18, 0, Integer.MAX_VALUE, Integer.MIN_VALUE};
    stashIntArray(buffer, expected);
    buffer.flip();
    int[] actual = spawnIntArray(buffer);
    assertNotSame(expected, actual);
    assertArrayEquals(expected, actual);
  }

  @Test
  void testStashViewLongArray() {
    long[] expected = {47, 11, 18, 0, Long.MAX_VALUE, Long.MIN_VALUE};
    stashLongArray(buffer, expected);
    buffer.flip();
    long[] actual = spawnLongArray(buffer);
    assertNotSame(expected, actual);
    assertArrayEquals(expected, actual);
  }

  @Test
  void testStashViewShortArray() {
    short[] expected = {47, 11, 18, 0, Short.MAX_VALUE, Short.MIN_VALUE};
    stashShortArray(buffer, expected);
    buffer.flip();
    short[] actual = spawnShortArray(buffer);
    assertNotSame(expected, actual);
    assertArrayEquals(expected, actual);
  }
}
