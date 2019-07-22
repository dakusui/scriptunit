package com.github.dakusui.scriptiveunit.loaders.preprocessing;

import java.util.List;

import static com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec.dict;
import static java.util.Objects.requireNonNull;

public interface Preprocessor {
  ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary rawScript);

//  ApplicationSpec.Dictionary readRawScript(String scriptResourceName);

  class Builder<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
    private ApplicationSpec applicationSpec;


    private final HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec;

    public Builder(HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
      this.hostSpec = requireNonNull(hostSpec);
    }

    public Builder<NODE, OBJECT, ARRAY, ATOM> applicationSpec(ApplicationSpec applicationSpec) {
      this.applicationSpec = requireNonNull(applicationSpec);
      return this;
    }

    public Preprocessor build() {
      requireNonNull(applicationSpec);
      requireNonNull(hostSpec);
      return new Preprocessor() {
        @Override
        public ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary rawScript) {
          ApplicationSpec.Dictionary ret = applicationSpec.deepMerge(
              preprocess(rawScript, applicationSpec.preprocessors()),
              applicationSpec.createDefaultValues());
          for (String parent : applicationSpec.parentsOf(rawScript)) {
            ret = applicationSpec.deepMerge(
                readApplicationDictionaryWithMerging(parent, applicationSpec),
                ret
            );
          }
          return applicationSpec.removeInheritanceDirective(ret);
        }

        ApplicationSpec.Dictionary readApplicationDictionaryWithMerging(
            String resourceName,
            ApplicationSpec applicationSpec) {
          ApplicationSpec.Dictionary resource = preprocess(
              hostSpec.readRawScript(resourceName),
              applicationSpec.preprocessors());

          ApplicationSpec.Dictionary work_ = dict();
          for (String s : applicationSpec.parentsOf(resource))
            work_ = applicationSpec.deepMerge(readApplicationDictionaryWithMerging(s, applicationSpec), work_);
          return applicationSpec.deepMerge(resource, work_);
        }

        ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary inputNode, List<PreprocessingUnit> preprocessingUnits) {
          for (PreprocessingUnit each : preprocessingUnits) {
            inputNode = ApplicationSpec.preprocess(inputNode, each);
          }
          return inputNode;
        }
      };
    }
  }
}
