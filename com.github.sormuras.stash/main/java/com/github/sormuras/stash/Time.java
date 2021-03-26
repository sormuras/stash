package com.github.sormuras.stash;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * First execution instant (time) annotation.
 *
 * <p>Usage: {@code void setData(String data, @Time long created)}
 *
 * @see System#currentTimeMillis()
 * @see java.time.Instant#ofEpochMilli(long)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Time {}
