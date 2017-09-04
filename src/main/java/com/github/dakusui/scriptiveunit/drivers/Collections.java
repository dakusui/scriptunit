package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Exceptions;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.github.dakusui.scriptiveunit.model.statement.Form;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.google.common.collect.Iterables;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

@SuppressWarnings("unused")
public class Collections {
  @SuppressWarnings("unused")
  @Scriptable
  public <E> Func<Integer> size(Func<Iterable<? extends E>> iterable) {
    return (Stage input) -> Iterables.size(requireNonNull(iterable.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Func<Integer> concat(Func<Iterable<? extends E>> iterable) {
    return (Stage input) -> Iterables.size(requireNonNull(iterable.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Func<Iterable<? extends E>> filter(Func<Iterable<? extends E>> iterable, Func<Function<E, Boolean>> predicate) {
    return (Stage i) -> {
      //noinspection unchecked
      return (Iterable<? extends E>) stream(
          requireNonNull(iterable.apply(i)).<E>spliterator(),
          false
      ).filter(
          input -> requireNonNull(requireNonNull(predicate.apply(i)).apply(input))
      ).collect(
          Collectors.<E>toList()
      );
    };
  }

  @Scriptable
  public <E> Func<Iterable<? extends E>> filter2(Func<Iterable<? extends E>> iterable, Func<Statement> predicate) {
    return (Stage i) -> {
      //noinspection unchecked
      return (Iterable<? extends E>) stream(
          requireNonNull(iterable.apply(i)).<E>spliterator(),
          false
      ).filter(
          entry -> (boolean) predicate.apply(Form.Utils.createWrappedStage(i, new Func[] { new Func() {
            @Override
            public Object apply(Stage input) {
              return entry;
            }

            @Override
            public Object apply(Object o) {
              throw Exceptions.I.impossibleLineReached();
            }
          } })).compile(FuncInvoker.create()).apply(i)
      ).collect(
          Collectors.<E>toList()
      );
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Func<Function<E, Boolean>> containedBy(Func<Iterable<E>> iterable) {
    return (Stage input) -> {
      Iterable<E> collection = requireNonNull(iterable.apply(input));
      return (Function<E, Boolean>) entry -> Iterables.contains(collection, entry);
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<Object> writeTo(Func<Map<String, Object>> map, Func<String> itemName, Func<Object> itemValue) {
    return input -> requireNonNull(map.apply(input)).put(itemName.apply(input), itemValue.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<Object> readFrom(Func<Map<String, Object>> map, Func<String> itemName) {
    return input -> requireNonNull(map.apply(input)).get(itemName.apply(input));
  }
}
