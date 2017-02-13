package com.github.dakusui.scriptunit.exceptions;

import com.github.dakusui.scriptunit.model.statement.Statement;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.function.Supplier;

import static java.lang.String.format;

public class SyntaxException extends ScriptUnitException {
  private SyntaxException(String message) {
    super(message);
  }

  public static Supplier<SyntaxException> attributeNotFound(String attributeName, Object context, Iterable<String> knownAttributeNames) {
    return () -> {
      throw new SyntaxException(format(
          "Attribute '%s' is accessed in '%s', but not found in your test case. Known attribute names are %s'",
          attributeName,
          context,
          knownAttributeNames));
    };
  }

  public static SyntaxException typeMismatch(Class type, Object input) {
    throw new SyntaxException(format("Type mismatch. '%s'(%s) couldn't be converted to %s",
        input,
        input != null ? input.getClass().getCanonicalName() : "none",
        type.getCanonicalName()
    ));
  }

  public static SyntaxException mergeFailed(ObjectNode source, ObjectNode target, String key) {
    throw new SyntaxException(format("Failed to merge '%s' and '%s' on '%s'", source, target, key));
  }

  public static SyntaxException nonObject(JsonNode jsonNode) {
    throw new SyntaxException(format("An object node was expected but not. '%s'", jsonNode));
  }

  public static <E extends ScriptUnitException> E nonArray(JsonNode jsonNode) {
    throw new SyntaxException(format("An array node was expected but not. '%s'", jsonNode));
  }

  public static <E extends ScriptUnitException> E nonText(JsonNode jsonNode) {
    throw new SyntaxException(format("A text node was expected but not. '%s'", jsonNode));
  }

  public static SyntaxException parameterNameShouldBeSpecifiedWithConstant(Statement.Nested statement) {
    throw new SyntaxException(format("Parameter name must be constant but not when accessor is used. (%s %s)", statement.getForm(), statement.getArguments()));
  }
}
