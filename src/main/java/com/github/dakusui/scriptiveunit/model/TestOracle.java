package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.loaders.Beans;
import com.github.dakusui.scriptiveunit.model.func.Form;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.scriptiveunit.core.Utils.append;
import static com.github.dakusui.scriptiveunit.core.Utils.iterableToString;
import static com.github.dakusui.scriptiveunit.core.Utils.template;
import static com.github.dakusui.scriptiveunit.model.func.FuncInvoker.createMemo;
import static com.github.dakusui.scriptiveunit.model.stage.Stage.ExecutionLevel.ORACLE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public interface TestOracle {
  static String templateTestOracleDescription(TestOracle testOracle, Tuple testCaseTuple, String testSuiteDescription) {
    return template(testOracle.getDescription(), append(testCaseTuple, "@TESTSUITE", testSuiteDescription));
  }

  int getIndex();

  /**
   * Returns a string that describes this test oracle.
   * <p>
   * Note that this method always returns raw form (a string before being templated).
   */
  String getDescription();

  String templateDescription(Tuple testCaseTuple, String testSuiteDescription);

  Function<Session, Action> createOracleActionFactory(TestItem testItem);

  interface Box {
    Function<Stage, Action> createBefore(TestItem testItem, Report report);

    Function<Stage, Matcher<Tuple>> givenFactory();

    Function<Stage, Object> whenFactory();

    Function<Stage, Sink<AssertionError>> errorHandlerFactory(TestItem testItem, Report report);

    Function<Stage, Action> createAfter(TestItem testItem, Report report);

    Function<Stage, Function<Object, Matcher<Stage>>> thenFactory();

    String describeTestCase(Tuple testCaseTuple);
  }

  class MyTestOracle implements TestOracle {
    private final int                 index;
    private final TestSuiteDescriptor testSuiteDescriptor;
    private final Statement.Factory   statementFactory;
    private       List<Object>        afterClause;
    private       List<Object>        beforeClause;
    private       String              description;
    private       List<Object>        givenClause;
    private       List<Object>        onFailureClause;
    private       List<Object>        thenClause;
    private       List<Object>        whenClause;

    MyTestOracle(final List<Object> afterClause, final List<Object> beforeClause, final String description, final List<Object> givenClause, final List<Object> onFailureClause, final List<Object> thenClause, final List<Object> whenClause, int index, TestSuiteDescriptor testSuiteDescriptor, Statement.Factory statementFactory, Session session) {
      this.index = index;
      this.testSuiteDescriptor = testSuiteDescriptor;
      this.statementFactory = statementFactory;
      this.afterClause = afterClause;
      this.beforeClause = beforeClause;
      this.description = description;
      this.givenClause = givenClause;
      this.onFailureClause = onFailureClause;
      this.thenClause = thenClause;
      this.whenClause = whenClause;
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public String templateDescription(Tuple testCaseTuple, String testSuiteDescription) {
      return TestOracle.templateTestOracleDescription(this, testCaseTuple, testSuiteDescription);
    }

    /**
     * Warning: Created action is not thread safe. Users must create 1 action for 1 thread.
     */
    @Override
    public Function<Session, Action> createOracleActionFactory(TestItem testItem) {
      return session1 -> session1.createOracleAction(testItem, createBox(testItem)
      );
    }

    private Box createBox(TestItem testItem) {
      return new Box() {
        private Map<List<Object>, Object> memo = createMemo();

        @Override
        public Function<Stage, Action> createBefore(TestItem testItem, Report report) {
          if (beforeClause == null)
            return s -> Actions.nop();
          Statement statement = statementFactory.create(beforeClause);
          FuncInvoker funcInvoker = FuncInvoker.create(memo);
          return s -> Beans.<Action>toFunc(statement, funcInvoker).apply(s);
        }

        @Override
        public Function<Stage, Matcher<Tuple>> givenFactory() {
          return (Stage s) -> new BaseMatcher<Tuple>() {
            private Statement givenStatement = statementFactory.create(givenClause);
            private FuncInvoker funcInvoker = FuncInvoker.create(memo);

            @Override
            public boolean matches(Object item) {
              return requireNonNull(Beans.<Boolean>toFunc(givenStatement, funcInvoker).apply(s));
            }

            @Override
            public void describeTo(Description description) {
              description.appendText(
                  format("input (%s) should have made true following criterion but not.:%n'%s' defined in stage:%s",
                      testItem.getTestCaseTuple(),
                      funcInvoker.asString(),
                      ORACLE));
            }
          };
        }

        @Override
        public Function<Stage, Object> whenFactory() {
          return new Function<Stage, Object>() {
            FuncInvoker funcInvoker = FuncInvoker.create(memo);

            @Override
            public Object apply(Stage s) {
              return Beans.<Boolean>toFunc(statementFactory.create(whenClause), funcInvoker).apply(s);
            }
          };
        }

        @Override
        public Function<Stage, Function<Object, Matcher<Stage>>> thenFactory() {
          Statement thenStatement = statementFactory.create(thenClause);
          FuncInvoker funcInvoker = FuncInvoker.create(memo);
          return stage -> out -> new BaseMatcher<Stage>() {
            Function<FuncInvoker, Predicate<Stage>> p = fi -> s -> requireNonNull(
                Beans.<Boolean>toFunc(thenStatement, fi).apply(s));
            Function<FuncInvoker, Function<Stage, String>> c = fi -> s -> fi.asString();

            @Override
            public boolean matches(Object item) {
              return p.apply(funcInvoker).test(stage);
            }

            @Override
            public void describeTo(Description description) {
              description.appendText(format("output should have made true the criterion defined in stage:%s", stage.getExecutionLevel()));
            }

            @Override
            public void describeMismatch(Object item, Description description) {
              Object output = out instanceof Iterable ?
                  iterableToString((Iterable<?>) out) :
                  out;
              description.appendText(String.format("output '%s' created from '%s' did not satisfy it.:%n'%s'",
                  output,
                  testItem.getTestCaseTuple(),
                  c.apply(funcInvoker).apply(stage)));
            }
          };
        }

        @Override
        public String describeTestCase(Tuple testCaseTuple) {
          return "Verify with: " + Utils.filterSimpleSingleLevelParametersOut(
              testCaseTuple,
              testSuiteDescriptor.getFactorSpaceDescriptor().getParameters()
          );
        }

        @Override
        public Function<Stage, Sink<AssertionError>> errorHandlerFactory(TestItem testItem, Report report) {
          Statement onFailureStatement = statementFactory.create(onFailureClause);
          FuncInvoker funcInvoker = FuncInvoker.create(memo);
          return (Stage s) -> (AssertionError input, Context context) -> requireNonNull(
              onFailureClause != null ?
                  Beans.<Action>toFunc(onFailureStatement, funcInvoker) :
                  (Form<Action>) input1 -> nop()).apply(s);
        }

        @Override
        public Function<Stage, Action> createAfter(TestItem testItem, Report report) {
          if (afterClause == null)
            return s -> Actions.nop();
          Statement statement = statementFactory.create(afterClause);
          FuncInvoker funcInvoker = FuncInvoker.create(memo);
          return s -> Beans.<Action>toFunc(statement, funcInvoker).apply(s);
        }
      };
    }

  }
}
