package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.format;

public interface Report extends Map<String, Object> {
  void submit();

  static Report create(Config config, TestItem testItem) {
    int testCaseId = testItem.getTestCaseId();
    int oracleId = testItem.getTestOracleId();
    return new Base() {
      @Override
      public void submit() {
        File reportFile = new File(ensureDirectoryExists(reportingDirectory()), config.getReportFileName());
        try {
          new ObjectMapper().writeValue(reportFile, this);
        } catch (IOException e) {
          throw ScriptiveUnitException.wrap(e, "Failed to write to a report file '%s'", reportFile);
        }
      }

      private File reportingDirectory() {
        return
            new File(
                new File("reports"),
                new File(
                    this.baseDirectory(),
                    new File(
                        Integer.toString(oracleId),
                        Integer.toString(testCaseId)
                    ).getPath()
                ).getPath()
            );
      }

      private File baseDirectory() {
        return config.getBaseDirectory();
      }

      private File ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
          Utils.check(dir.mkdirs(), () -> new ScriptiveUnitException(format("Failed to create a directory '%s'.", dir)));
          return dir;
        }
        return Utils.check(dir, File::isDirectory, () -> new ScriptiveUnitException(format("'%s' exists, but not a directory.", dir)));
      }
    };
  }

  abstract class Base extends TreeMap<String, Object> implements Report {
  }
}
