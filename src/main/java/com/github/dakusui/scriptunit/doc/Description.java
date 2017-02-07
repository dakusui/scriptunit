package com.github.dakusui.scriptunit.doc;

import java.util.Collections;
import java.util.List;

public interface Description {
  String name();

  List<String> content();

  default List<Description> children() {
    return Collections.emptyList();
  }
}
