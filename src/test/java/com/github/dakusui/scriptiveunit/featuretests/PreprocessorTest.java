package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;
import com.github.dakusui.scriptiveunit.utils.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

public class PreprocessorTest {
  @Test
  public void test() {
    ObjectNode rawObjectNode = toObjectNode(createDictionary());
    ApplicationSpec applicationSpec = createApplicationSpec();
    HostSpec.Json hostSpec = createHostSpec();
    ResourceStoreSpec resourceStoreSpec = createResourceStoreSpec();
    ObjectNode preprocessedObjectNode = preprocess(rawObjectNode, applicationSpec, hostSpec, resourceStoreSpec);
    System.out.println(preprocessedObjectNode);
  }

  private ResourceStoreSpec createResourceStoreSpec() {
    return new ResourceStoreSpec.Impl(new JsonUtils.NodeFactory<ObjectNode>(){
      @Override
      public JsonNode create() {
        return obj();
      }
    }.get());
  }

  private ApplicationSpec.Standard createApplicationSpec() {
    return new ApplicationSpec.Standard();
  }

  private HostSpec.Json createHostSpec() {
    return new HostSpec.Json();
  }

  private ApplicationSpec.Dictionary createDictionary() {
    return new ApplicationSpec.Dictionary.Factory() {
      ApplicationSpec.Dictionary create() {
        return dict($("hello", "world"));
      }
    }.create();
  }


  private static ObjectNode preprocess(ObjectNode objectNode, ApplicationSpec applicationSpec, HostSpec.Json hostSpec, ResourceStoreSpec resourceStoreSpec) {
    return toObjectNode(createPreprocessor(hostSpec, applicationSpec)
        .preprocess(hostSpec.toApplicationDictionary(objectNode), resourceStoreSpec));
  }

  private static Preprocessor createPreprocessor(HostSpec.Json hostSpec, ApplicationSpec applicationSpec) {
    return new Preprocessor.Builder(hostSpec)
        .applicationSpec(applicationSpec)
        .build();
  }

  private static ObjectNode toObjectNode(ApplicationSpec.Dictionary dictionary) {
    return new HostSpec.Json().toHostObject(dictionary);
  }
}
