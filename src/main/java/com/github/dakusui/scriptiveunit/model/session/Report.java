package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.utils.Checks;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.format;

public interface Report extends Map<String, Object> {
  void submit();

  static Report create(File baseDir, final File applicationDirectory, final String testSuiteName, TestItem testItem, final String reportFileName) {
    int testCaseId = testItem.getTestCaseId();
    int oracleId = testItem.getTestOracleId();
    return new Base() {
      private final File base = baseDir;

      @Override
      public void submit() {
        File reportFile = new File(ensureDirectoryExists(reportingDirectory()), reportFileName);
        try {
          new ObjectMapper().writeValue(reportFile, this);
        } catch (IOException e) {
          throw ScriptiveUnitException.wrap(e, "Failed to write to a report file '%s'", reportFile);
        }
      }

      private File reportingDirectory() {
        return base == null ?
            reportingDirectory_() :
            new File(base, reportingDirectory_().getPath());
      }
      private File reportingDirectory_() {
        return new File(
            new File("reports"),
            new File(testSuiteName,
                new File(
                    this.applicationDirectory(),
                    new File(
                        Integer.toString(oracleId),
                        Integer.toString(testCaseId)
                    ).getPath()
                ).getPath()
            ).getPath()
        );
      }

      private File applicationDirectory() {
        return applicationDirectory;
      }

      private File ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
          Checks.check(dir.mkdirs(), () -> new ScriptiveUnitException(format("Failed to create a directory '%s'.", dir)));
          return dir;
        }
        return Checks.check(dir, File::isDirectory, () -> new ScriptiveUnitException(format("'%s' exists, but not a directory.", dir)));
      }
    };
  }

  abstract class Base extends TreeMap<String, Object> implements Report {
  }
}
