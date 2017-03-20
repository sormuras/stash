package de.sormuras.stash.compiler;

import de.sormuras.beethoven.unit.MethodDeclaration;
import de.sormuras.beethoven.unit.MethodParameter;

public enum Tag {
  METHOD_IS_BASE,
  METHOD_IS_CHAINABLE,
  METHOD_IS_VOLATILE,
  METHOD_IS_DIRECT,
  PARAMETER_IS_ENUM,
  PARAMETER_IS_STASHABLE,
  PARAMETER_IS_TIME;

  public static boolean isMethodChainable(MethodDeclaration method) {
    return Boolean.TRUE.equals(method.getTags().get(METHOD_IS_CHAINABLE));
  }

  public static boolean isMethodVolatile(MethodDeclaration method) {
    return Boolean.TRUE.equals(method.getTags().get(METHOD_IS_VOLATILE));
  }

  public static boolean isMethodReturn(MethodDeclaration method) {
    return !method.getReturnType().isVoid();
  }

  public static boolean isParameterTime(MethodParameter parameter) {
    return Boolean.TRUE.equals(parameter.getTags().get(PARAMETER_IS_TIME));
  }
}
