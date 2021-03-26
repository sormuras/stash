package com.github.sormuras.stash;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Prevalent System Interface annotation. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Stash {

  /** {@return super class of the generated stash class} */
  Class<?> classExtends() default Object.class;

  /**
   * {@return comments to be copied into the {@link javax.annotation.processing.Generated}
   * annotation}
   */
  String comments() default "";

  /** {@return {@code true} to generate stash/spawn verification code, {@code false} to omit it} */
  boolean verify() default true;
}
