package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.scriptiveunit.core.Config;

public enum TestUtils {
  ;

  public static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  public static void configureScriptNameSystemProperty(String scriptName, Class driverClass) {
    String scriptSystemPropertyKey = Config.create(driverClass, System.getProperties()).getScriptSystemPropertyKey();
    System.setProperty(scriptSystemPropertyKey, scriptName);
  }
}