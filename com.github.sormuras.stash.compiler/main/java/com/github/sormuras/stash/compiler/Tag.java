package com.github.sormuras.stash.compiler;

import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.beethoven.unit.MethodDeclaration;
import com.github.sormuras.beethoven.unit.MethodParameter;

public enum Tag {
  METHOD_IS_BASE,
  METHOD_IS_CHAINABLE,
  METHOD_IS_VOLATILE,
  METHOD_IS_DIRECT,

  TYPE_IS_ENUM,
  TYPE_IS_STASHABLE,
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

  public static boolean isTypeEnum(Type type) {
    return Boolean.TRUE.equals(type.getTags().get(TYPE_IS_ENUM));
  }

  public static boolean isTypeStashable(Type type) {
    return Boolean.TRUE.equals(type.getTags().get(TYPE_IS_STASHABLE));
  }

  public static boolean isParameterTime(MethodParameter parameter) {
    return Boolean.TRUE.equals(parameter.getTags().get(PARAMETER_IS_TIME));
  }

  static void setMethodIsBase(MethodDeclaration method, boolean isBase) {
    method.getTags().put(METHOD_IS_BASE, isBase);
  }

  static void setMethodIsChainable(MethodDeclaration method, boolean isChainable) {
    method.getTags().put(METHOD_IS_CHAINABLE, isChainable);
  }

  static void setMethodIsVolatile(MethodDeclaration method, boolean isVolatile) {
    method.getTags().put(METHOD_IS_VOLATILE, isVolatile);
  }

  static void setMethodIsDirect(MethodDeclaration method, boolean isDirect) {
    method.getTags().put(METHOD_IS_DIRECT, isDirect);
  }

  static void setTypeIsEnum(Type type, boolean isEnum) {
    type.getTags().put(TYPE_IS_ENUM, isEnum);
  }

  static void setTypeIsStashable(Type type, boolean isStashable) {
    type.getTags().put(TYPE_IS_STASHABLE, isStashable);
  }

  static void setParameterIsTime(MethodParameter parameter, boolean isTime) {
    parameter.getTags().put(PARAMETER_IS_TIME, isTime);
  }
}
