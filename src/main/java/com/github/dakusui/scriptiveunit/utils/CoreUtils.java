package com.github.dakusui.scriptiveunit.utils;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static com.github.dakusui.scriptiveunit.core.Exceptions.SCRIPTIVEUNIT;
import static java.math.MathContext.DECIMAL128;

public enum CoreUtils {
  ;
  public static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new ImmutableMap.Builder<Class<?>, Class<?>>()
      .put(boolean.class, Boolean.class).put(byte.class, Byte.class).put(char.class, Character.class)
      .put(double.class, Double.class).put(float.class, Float.class).put(int.class, Integer.class)
      .put(long.class, Long.class).put(short.class, Short.class).put(void.class, Void.class).build();

  public static BigDecimal toBigDecimal(Number number) {
    if (number instanceof BigDecimal)
      return (BigDecimal) number;
    return new BigDecimal(number.toString(), DECIMAL128);
  }

  public static Object toBigDecimalIfPossible(Object object) {
    if (object instanceof Number) {
      return toBigDecimal((Number) object);
    }
    return object;
  }

  public static <T> T car(T[] arr) {
    return SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr))[0];
  }

  public static <T> T[] cdr(T[] arr) {
    return Arrays.copyOfRange(
        SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr)),
        1,
        arr.length
    );
  }

  public static Object[] shrinkTo(Class<?> componentType, int count, Object[] args) {
    Object[] ret = new Object[count];
    Object var = Array.newInstance(componentType, args.length - count + 1);
    if (count > 1) {
      System.arraycopy(args, 0, ret, 0, ret.length - 1);
    }
    //noinspection SuspiciousSystemArraycopy
    System.arraycopy(args, ret.length - 1, var, 0, args.length - count + 1);
    ret[ret.length - 1] = var;
    return ret;
  }
}
