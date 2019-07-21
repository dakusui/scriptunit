package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.annotations.Utils;
import com.github.dakusui.scriptiveunit.annotations.Value;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;

import static java.util.Objects.requireNonNull;

public interface ScriptLoader {
  JsonScript load(Class<?> driverClass);

  abstract class Base implements ScriptLoader {
  }

  class FromResource extends Base {
    private final String scriptResourceName;

    public FromResource(String scriptResourceName) {
      this.scriptResourceName = scriptResourceName;
    }

    @Override
    public JsonScript load(Class<?> driverClass) {
      return DriverUtils.createJsonScriptFromResource(driverClass, scriptResourceName);
    }
  }

  class FromResourceSpecifiedBySystemProperty extends FromResource {
    private static final String DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY = "scriptiveunit.target";

    @SuppressWarnings("WeakerAccess")
    public FromResourceSpecifiedBySystemProperty(String systemPropertyKey) {
      super(System.getProperties().getProperty(requireNonNull(systemPropertyKey)));
    }

    @SuppressWarnings("unused")
    public FromResourceSpecifiedBySystemProperty() {
      this(System.getProperties().getProperty(DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY));
    }

    public static String getScriptResourceNameKey(RunScript runScriptAnnotation) {
      Class<? extends ScriptLoader> scriptLoaderClass = runScriptAnnotation.loader().value();
      if (scriptLoaderClass.isAssignableFrom(FromResourceSpecifiedBySystemProperty.class)) {
        Value[] args = runScriptAnnotation.loader().args();
        if (args.length > 0)
          return (String) Utils.argValues(args)[0];
        else
          return DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;
      }
      throw new RuntimeException();
    }

    public static String getScriptResourceNameKey(Class<?> driverClass) {
      return getScriptResourceNameKey(ReflectionUtils.getAnnotation(driverClass, RunScript.class)
          .orElseThrow(RuntimeException::new));
    }
  }
}