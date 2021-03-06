package com.github.dakusui.scriptiveunit.drivers.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormList;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.utils.ActionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public class Basic {
  private static final Logger LOGGER = LoggerFactory.getLogger(Basic.class);

  private static Consumer<String> out = System.err::println;//LOGGER::debug;

  public static void setOut(Consumer<String> out) {
    Basic.out = requireNonNull(out);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> nop() {
    return input -> ActionSupport.nop();
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Action> sequential(FormList<Action> actions) {
    return (Stage input) -> ActionSupport.sequential(
        actions.stream()
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.size()]));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Action> concurrent(FormList<Action> actions) {
    return (Stage input) -> ActionSupport.parallel(
        actions.stream()
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.size()]));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> fail(Form<String> in) {
    return input -> simple("fail", (c) -> {
      throw new RuntimeException(in.apply(input));
    });
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> print(Form<?> in) {
    return input -> leaf(
        ContextConsumer.of(() -> "print", c -> {
          String s = in.apply(input).toString();
          System.out.println(s);

        })
    );
  }

  @Doc("Consumes a string composed from a given string 'msg' and given an input value " +
      "using the static field 'out' and returns the value.")
  @SuppressWarnings("unused")
  @Scriptable
  public <T> Form<T> debug(Form<String> msg, Form<T> in) {
    return input -> {
      T ret = in.apply(input);
      out.accept(msg.apply(input) + ":" + ret);
      return ret;
    };
  }

  @Doc("Prints to a given value to a 'dumb' output, which doesn't do anything.")
  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> dumb(@Doc("A value to be printed") Form<?> in) {
    return (Stage input) -> simple(
        "dumb",
        // Even if it doesn't go anywhere, we must do 'apply'.
        (c) -> in.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Action> tag(Form<String> string) {
    return input -> simple(
        Objects.toString(string.apply(input)),
        (c) -> {
        });
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Boolean> perform(FormList<Action> actions) {
    return input -> {
      ActionUtils.performActionWithLogging(
          ActionSupport.sequential(
              actions.stream()
                  .map(actionFunc -> actionFunc.apply(input))
                  .collect(toList()))
      );
      return true;
    };
  }
}
