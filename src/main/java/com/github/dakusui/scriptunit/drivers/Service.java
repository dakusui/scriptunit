package com.github.dakusui.scriptunit.drivers;

import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.model.Func;
import com.github.dakusui.scriptunit.model.Stage;

import java.util.Map;

public abstract class Service<REQUEST, RESPONSE> extends Core {
  @Scriptable
  public <T extends Stage> Func.Memoized<T, RESPONSE> service(Func<T, REQUEST> request) {
    return input -> Service.this.service(request.apply(input));
  }

  @Scriptable
  public <T extends Stage> Func<T, REQUEST> with(Func<T, Map<String, Object>> values, Func<T, REQUEST> request) {
    return input -> override(values.apply(input), request.apply(input));
  }

  @Scriptable
  public <T extends Stage> Func<T, REQUEST> request() {
    return input -> buildRequest(input.getTestCaseTuple());
  }

  @Scriptable
  public <T extends Stage> Func<T, RESPONSE> response() {
    //noinspection unchecked
    return input -> (RESPONSE) input.response();
  }

  /**
   * @param fixture A test case.
   */
  abstract protected REQUEST buildRequest(Map<String, Object> fixture);

  abstract protected RESPONSE service(REQUEST request);

  abstract protected REQUEST override(Map<String, Object> values, REQUEST request);

}
