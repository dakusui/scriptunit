package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.github.dakusui.scriptiveunit.model.form.handle.FormUtils;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

enum SessionUtils {
  ;

  /**
   * This method returns a list of parameter names used inside a given by looking
   * into inside the statement.
   * Parameter names are names of factors defined in the test suite descriptor.
   *
   * @param statement Statement to be looked into
   * @return A list of factor names.
   */
  static List<String> involvedParameters(Statement statement) {
    requireNonNull(statement);
    List<String> ret = Lists.newLinkedList();
    return involvedParameters(statement, ret);
  }

  private static List<String> involvedParameters(Statement statement, List<String> work) {
    if (statement instanceof Statement.Atom)
      return work;
    if (statement instanceof Statement.Compound) {
      if (((Statement.Compound) statement).getFormHandle().isAccessor()) {
        for (Statement each : ((Statement.Compound) statement).getArguments()) {
          if (each instanceof Statement.Atom) {
            /*
             * Since this method needs to look into the internal structure of
             * the statement by evaluating it, it is valid to pass a fresh
             * memo object to an invoker.
             */
            work.add(Objects.toString(FormUtils.INSTANCE.toForm(each)));
          } else {
            throw SyntaxException.parameterNameShouldBeSpecifiedWithConstant((Statement.Compound) statement);
          }
        }
      } else {
        for (Statement each : ((Statement.Compound) statement).getArguments()) {
          work = involvedParameters(each, work);
        }
      }
    }
    return work;
  }
}
