package com.github.dakusui.scriptiveunit.testutils.drivers;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteLoader;
import com.github.dakusui.scriptiveunit.testutils.Resource;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;


public abstract class Loader extends JsonBasedTestSuiteLoader {
  @SuppressWarnings("WeakerAccess")
  protected Loader(Config config) {
    super(config);
  }

  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode work = super.readObjectNodeWithMerging(resourceName);
    for (ObjectNode each : objectNodes()) {
      work = Utils.deepMerge(each, work);
    }
    return work;
  }

  protected abstract ObjectNode[] objectNodes();

  @SafeVarargs
  public static Loader create(Config config, Resource<ObjectNode>... resources) {
    return new Loader(config) {
      @Override
      protected ObjectNode[] objectNodes() {
        return Arrays.stream(resources).map(Resource::get).collect(toList()).toArray(new ObjectNode[resources.length]);
      }
    };
  }
}
