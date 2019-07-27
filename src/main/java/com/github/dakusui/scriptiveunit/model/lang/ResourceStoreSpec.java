package com.github.dakusui.scriptiveunit.model.lang;

import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.node.ObjectNode;

import java.io.InputStream;

import static com.github.dakusui.scriptiveunit.utils.JsonUtils.readJsonNodeFromStream;
import static com.github.dakusui.scriptiveunit.utils.JsonUtils.requireObjectNode;

/**
 * An interface that represents a "resource store", from which scripts are read.
 */
public interface ResourceStoreSpec {

  InputStream openResource(String resourceName);

  ObjectNode readObjectNode(String resourceName);

  ObjectNode mainNode();

  abstract class Base implements ResourceStoreSpec {
    @Override
    public InputStream openResource(String resourceName) {
      return ReflectionUtils.openResourceAsStream(resourceName);
    }

    @Override
    public ObjectNode readObjectNode(String resourceName) {
      return requireObjectNode(readJsonNodeFromStream(this.openResource(resourceName)));
    }

  }

  class Impl extends Base {
    private final ObjectNode mainNode;

    public Impl(ObjectNode mainNode) {
      this.mainNode = mainNode;
    }

    public Impl(String mainNodeResourceName) {
      this.mainNode = readObjectNode(mainNodeResourceName);
    }

    @Override
    public ObjectNode mainNode() {
      return this.mainNode;
    }
  }
}
