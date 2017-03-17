package de.sormuras.stash;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Stash {

  /** Prevalent System Interface annotation. */
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Interface {

    /** @return super class of the generated stash class */
    Class<?> classExtends() default Object.class;

    /** @return {@code false} to omit generation of verification, defaults to {@code true}. */
    boolean verify() default true;
  }
}
