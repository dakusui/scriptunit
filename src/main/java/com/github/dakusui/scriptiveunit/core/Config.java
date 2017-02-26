package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.Load;

import java.io.File;
import java.util.Properties;

import static com.github.dakusui.scriptiveunit.exceptions.ConfigurationException.scriptNotSpecified;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;

public interface Config {
  Class<?> getDriverClass();

  Object getDriverObject();

  String getScriptResourceNameKey();

  String getScriptResourceName();

  File getBaseDirectory();

  String getReportFileName();

  class Builder {
    private final Properties properties;
    final         Load       loadAnnotation;
    private final Class<?>   driverClass;

    public Builder(Class<?> driverClass, Properties properties) {
      this.driverClass = driverClass;
      this.properties = new Properties();
      this.properties.putAll(properties);
      this.loadAnnotation = Utils.getAnnotation(driverClass, Load.class, Load.DEFAULT_INSTANCE);
    }

    public Builder withScriptResourceName(String scriptResourceName) {
      this.properties.put(loadAnnotation.scriptSystemPropertyKey(), scriptResourceName);
      return this;
    }

    public Config build() {
      try {
        return new Config() {
          Object driverObject = Builder.this.driverClass.newInstance();

          @Override
          public Class<?> getDriverClass() {
            return Builder.this.driverClass;
          }

          @Override
          public Object getDriverObject() {
            return driverObject;
          }

          @Override
          public String getScriptResourceNameKey() {
            return loadAnnotation.scriptSystemPropertyKey();
          }

          @Override
          public String getScriptResourceName() {
            return Utils.check(
                properties.getProperty(getScriptResourceNameKey(), Builder.this.loadAnnotation.defaultScriptName()),
                (in) -> !in.equals(Load.SCRIPT_NOT_SPECIFIED),
                () -> scriptNotSpecified(getScriptResourceNameKey())
            );
          }

          @Override
          public File getBaseDirectory() {
            return new File(".");
          }

          @Override
          public String getReportFileName() {
            return "report.json";
          }
        };
      } catch (InstantiationException | IllegalAccessException e) {
        throw wrap(e);
      }
    }
  }
}
