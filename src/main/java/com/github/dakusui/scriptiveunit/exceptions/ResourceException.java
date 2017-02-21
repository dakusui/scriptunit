package com.github.dakusui.scriptiveunit.exceptions;

import static java.lang.String.format;

public class ResourceException extends ScriptiveUnitException {
  public ResourceException(String message) {
    super(message);
  }

  public static ResourceException scriptNotFound(String scriptName) {
    throw new ResourceException(format("Script '%s' was not found. Check your classpath.", scriptName));
  }

}