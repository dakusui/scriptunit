package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.scriptiveunit.model.session.Pipe;
import com.github.dakusui.scriptiveunit.model.session.Sink;
import com.github.dakusui.scriptiveunit.model.session.Source;
import com.google.common.collect.Iterables;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.cyclicTemplatingFound;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.undefinedFactor;
import static java.lang.Character.*;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public enum StringUtils {
  ;

  public static String iterableToString(Iterable<?> i) {
    if (Iterables.size(i) < 2) {
      return i.toString();
    }
    StringBuilder b = new StringBuilder();
    b.append("[\n");
    i.forEach((Object in) -> {
      b.append("  ");
      b.append(in);
      b.append("\n");
    });
    b.append("]");
    return b.toString();
  }

  public static Runnable prettify(String prettyString, Runnable runnable) {
    return new Runnable() {
      @Override
      public void run() {
        runnable.run();
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T> Supplier<T> prettify(String prettyString, Supplier<T> supplier) {
    return new Supplier<T>() {
      @Override
      public T get() {
        return supplier.get();
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T> Source<T> prettify(String prettyString, Source<T> source) {
    return new Source<T>() {
      @Override
      public T apply(Context context) {
        return source.apply(context);
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T, U> Pipe<T, U> prettify(String prettyString, Pipe<T, U> pipe) {
    return new Pipe<T, U>() {

      @Override
      public U apply(T t, Context context) {
        return pipe.apply(t, context);
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T> Predicate<T> prettify(String prettyString, Predicate<T> predicate) {
    return new Predicate<T>() {

      @Override
      public boolean test(T t) {
        return predicate.test(t);
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T> Sink<T> prettify(String prettyString, Sink<T> sink) {
    return new Sink<T>() {

      @Override
      public void accept(T t, Context context) {
        sink.accept(t, context);
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static String toALL_CAPS(String inputString) {
    StringBuilder b = new StringBuilder();
    boolean wasPreviousUpper = true;
    for (Character each : inputString.toCharArray()) {
      boolean isUpper = isUpperCase(each);
      if (isUpper) {
        if (!wasPreviousUpper)
          b.append("_");
        b.append(each);
      } else {
        b.append(each.toString().toUpperCase());
      }
      wasPreviousUpper = isUpper;
    }
    return b.toString();
  }

  public static String toCamelCase(String inputString) {
    StringBuilder b = new StringBuilder();
    boolean wasPreviousUnderscore = false;
    for (Character each : inputString.toCharArray()) {
      boolean isUnderscore = each.equals('_');
      if (!isUnderscore) {
        if (wasPreviousUnderscore) {
          b.append(toUpperCase(each));
        } else {
          b.append(toLowerCase(each));
        }
      }
      wasPreviousUnderscore = isUnderscore;
    }
    return b.toString();
  }

  public static String template(String s, Map<String, Object> map) {
    String ret = s;
    Pattern pattern = Pattern.compile("\\{\\{(?<keyword>@?[A-Za-z_][A-Za-z0-9_]*)}}");
    Matcher matcher;
    int i = 0;
    while ((matcher = pattern.matcher(ret)).find()) {
      String keyword = matcher.group("keyword");
      Checks.check(i++ < map.size(), () -> cyclicTemplatingFound(format("template(%s)", s), map));
      Checks.check(map.containsKey(keyword), () -> undefinedFactor(keyword, format("template(%s)", s)));
      ret = ret.replaceAll(format("\\{\\{%s\\}\\}", keyword), requireNonNull(map.get(keyword)).toString());
    }
    return ret;
  }
}
