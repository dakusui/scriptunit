package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.Preprocessor;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.requireObjectNode;
import static java.util.stream.Collectors.toList;

public class JsonBasedTestSuiteDescriptorLoader extends TestSuiteDescriptorLoader.Base {

  /**
   * A resource that holds default values of ScriptiveUnit.
   */
  protected static final String DEFAULTS_JSON = "defaults/values.json";

  private final HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode> hostLanguage = hostLanguage();

  private final ModelSpec<JsonNode> modelSpec = modelSpec();

  @SuppressWarnings("unused")
  public JsonBasedTestSuiteDescriptorLoader(Config config) {
    super(config);
  }

  // TEMPLATE
  @Override
  public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
    return hostLanguage.mapObjectNode(
        readScriptHandlingInheritance(session.getConfig().getScriptResourceName()),
        JsonTestSuiteDescriptorBean.class)
        .create(session);
  }

  // TEMPLATE
  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode work = hostLanguage.newObjectNode();
    ObjectNode child = preprocess(hostLanguage.readObjectNode(resourceName), getPreprocessors());
    if (hostLanguage.hasInheritanceDirective(child))
      hostLanguage
          .getParents(child)
          .forEach(s -> hostLanguage.deepMerge(readObjectNodeWithMerging(s), work));
    return hostLanguage.deepMerge(child, work);
  }

  // TEMPLATE
  protected List<Preprocessor<JsonNode>> getPreprocessors() {
    return modelSpec.preprocessors_(hostLanguage)
        .stream()
        .map((Function<Preprocessor<ModelSpec.Node>, Preprocessor<JsonNode>>) nodePreprocessor -> new Preprocessor<JsonNode>() {
          @Override
          public JsonNode translate(JsonNode targetElement) {
            return hostLanguage.translate(nodePreprocessor.translate(hostLanguage.toModelNode(targetElement)));
          }

          @Override
          public boolean matches(Path pathToTargetElement) {
            return nodePreprocessor.matches(pathToTargetElement);
          }
        })
        .collect(toList())
        ;
  }

  // TEMPLATE
  protected ObjectNode readScriptHandlingInheritance(String scriptResourceName) {
    ObjectNode work = readObjectNodeWithMerging(scriptResourceName);
    ObjectNode ret = hostLanguage.translate(modelSpec.createDefaultValues());
    return hostLanguage.removeInheritanceDirective(hostLanguage.deepMerge(work, ret));
  }

  // TEMPLATE
  protected ObjectNode preprocess(ObjectNode inputNode, List<Preprocessor<JsonNode>> preprocessors) {
    for (Preprocessor<JsonNode> each : preprocessors) {
      inputNode = hostLanguage.translate(
          ModelSpec.preprocess(hostLanguage.toModelDictionary(inputNode), hostLanguage.convertProcessor(each)));
    }
    return requireObjectNode(inputNode);
  }

  protected ModelSpec<JsonNode> modelSpec() {
    return new ModelSpec.Standard<>();
  }

  protected HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode> hostLanguage() {
    return new HostLanguage.Json();
  }
}
