package com.github.sormuras.stash;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Positive (and small) natural number annotation.
 *
 * <pre>ℕ</pre>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Natural_number">Natural number</a>
 * @see Stashable.Buffer#stashIntN(java.nio.ByteBuffer, int)
 * @see Stashable.Buffer#stashLongN(java.nio.ByteBuffer, long)
 */
@Target({ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface N {}
