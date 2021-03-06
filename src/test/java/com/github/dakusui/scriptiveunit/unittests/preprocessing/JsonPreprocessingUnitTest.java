package com.github.dakusui.scriptiveunit.unittests.preprocessing;

import com.github.dakusui.scriptiveunit.loaders.preprocessing.PreprocessingUnit;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JsonPreprocessingUnitTest {
  @Test
  public void whenPreprocessingOnArrayIsRequested() throws IOException {
    ObjectNode targetObject = (ObjectNode) new ObjectMapper().readTree("{\"a1\":[0,1,2]}");
    PreprocessingUnit jsonPreprocessingUnit = new PreprocessingUnit() {
      @Override
      public ApplicationSpec.Node translate(ApplicationSpec.Node targetElement) {
        return ApplicationSpec.dict(
            ApplicationSpec.$("v1", ApplicationSpec.atom("Hello")),
            ApplicationSpec.$("v2", targetElement)
        );
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return PreprocessingUnit.Utils.pathComponentList("a1").equals(
            pathToTargetElement.asComponentList()
        );
      }
    };
    HostSpec.Json hostLanguage = new HostSpec.Json();
    assertEquals(
        "{\"a1\":{\"v1\":\"Hello\",\"v2\":[0,1,2]}}",
        hostLanguage.toHostObject(ApplicationSpec.preprocess(hostLanguage.toApplicationDictionary(targetObject), jsonPreprocessingUnit)).toString()
    );
  }

  @Test
  public void whenPreprocessingOnMapIsRequested() throws IOException {
    ObjectNode targetObject = (ObjectNode) new ObjectMapper().readTree("{\"a1\":{\"c1\":100, \"c2\":200}}");
    PreprocessingUnit jsonPreprocessingUnit = new PreprocessingUnit() {
      @Override
      public ApplicationSpec.Node translate(ApplicationSpec.Node targetElement) {
        return ApplicationSpec.dict(
            ApplicationSpec.$("v1", ApplicationSpec.atom("Hello")),
            ApplicationSpec.$("v2", targetElement)
        );
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return PreprocessingUnit.Utils.pathComponentList("a1", "c2").equals(
            pathToTargetElement.asComponentList()
        );
      }
    };
    HostSpec.Json hostLanguage = new HostSpec.Json();
    assertEquals(
        "{\"a1\":{\"c1\":100,\"c2\":{\"v1\":\"Hello\",\"v2\":200}}}",
        hostLanguage.toHostObject(ApplicationSpec.preprocess(hostLanguage.toApplicationDictionary(targetObject), jsonPreprocessingUnit)).toString()
    );
  }

}
