package com.github.dakusui.scriptiveunit.unittests.utils;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.scriptiveunit.model.session.action.Pipe;
import com.github.dakusui.scriptiveunit.model.session.action.Sink;
import com.github.dakusui.scriptiveunit.model.session.action.Source;
import com.github.dakusui.scriptiveunit.utils.StringUtils;
import org.junit.Test;

import java.util.function.Supplier;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;

public class StringUtilsTest {
  @Test
  public void testRunnable() {
    Runnable runnable = StringUtils.prettify("hello", () -> {
    });

    assertThat(
        runnable.toString(),
        asString().equalTo("hello").$());
  }

  @Test
  public void testSink() {
    Sink<String> runnable = StringUtils.prettify("hello", (s, context) -> {
    });

    assertThat(
        runnable.toString(),
        asString().equalTo("hello").$());
  }

  @Test
  public void testSource() {
    Source<String> source = StringUtils.prettify("hello", (Source<String>) context -> "HELLO");

    assertThat(
        source,
        allOf(
            asString("apply", Context.create()).equalTo("HELLO").$(),
            asString("toString").equalTo("hello").$()
        ));
  }

  @Test
  public void testSupplier() {
    Supplier<String> supplier = StringUtils.prettify("hello", () -> "HELLO");

    assertThat(
        supplier,
        allOf(
            asString("get").equalTo("HELLO").$(),
            asString("toString").equalTo("hello").$()
        ));
  }


  @Test
  public void testPipe() {
    Pipe<String, String> pipe = StringUtils.prettify("hello", (s, c) -> "HELLO:<" + s + ">");

    assertThat(
        pipe,
        allOf(
            asString("apply", "world", Context.create())
                .equalTo("HELLO:<world>").$(),
            asString("toString").equalTo("hello").$()
        ));
  }

  @Test
  public void givenNull$whenArrayToString$thenNA() {
    String s = StringUtils.arrayToString(null);
    System.out.println(s);
  }
}
