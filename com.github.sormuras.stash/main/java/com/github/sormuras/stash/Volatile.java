package com.github.sormuras.stash;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Volatile method annotation.
 *
 * <p>Usage: {@code @Volatile void fireOnce(Pay load, Other data)}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Volatile {
  /**
   * @return {@code true} if no synchronization is to be enforced executing the annotated method,
   *     defaults to {@code false}, i.e. every volatile method will acquire read-lock first.
   */
  boolean direct() default false;
}
