package com.github.dakusui.scriptiveunit.model.statement;

import java.util.Iterator;
import java.util.List;

public interface Arguments extends Iterable<Statement> {
  Statement get(int i);

  int size();

  static Arguments create(Statement.Factory statementFactory, List<Object> args) {
    return new Arguments() {
      public Statement get(int i) {
        return statementFactory.create(args.get(i));
      }

      @Override
      public int size() {
        return args.size();
      }

      @SuppressWarnings("NullableProblems")
      @Override
      public Iterator<Statement> iterator() {
        return new Iterator<Statement>() {
          Iterator<Object> i = args.iterator();

          @Override
          public boolean hasNext() {
            return i.hasNext();
          }

          @Override
          public Statement next() {
            return statementFactory.create(i.next());
          }
        };
      }
    };
  }
}
