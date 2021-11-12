package i2.act.examples.minic.interpreter;

import i2.act.examples.minic.bugs.Bug;
import i2.act.examples.minic.bugs.Bugs;
import i2.act.examples.minic.errors.InvalidProgramException;
import i2.act.examples.minic.frontend.ast.*;
import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.semantics.symbols.Symbol;
import i2.act.examples.minic.frontend.semantics.types.AtomicType;
import i2.act.examples.minic.frontend.semantics.types.FunctionType;
import i2.act.examples.minic.frontend.semantics.types.Type;
import i2.act.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Interpreter implements ASTVisitor<Interpreter.State, Interpreter.Value> {

  public static final int UNBOUNDED = -1;

  public static final class Timeout extends RuntimeException {

    // intentionally left blank

  }

  public static final Pair<Value, List<Value>> interpret(final Program program) {
    return interpret(program, UNBOUNDED, UNBOUNDED);
  }

  public static final Pair<Value, List<Value>> interpret(final Program program,
      final int maxNumberOfSteps, final int maxNumberOfLoopIterations) {
    final State state = new State();
    final Interpreter interpreter =
        new Interpreter(false, maxNumberOfSteps, maxNumberOfLoopIterations);

    final Value exitValue = interpreter.visit(program, state);

    return new Pair<Value, List<Value>>(exitValue, state.getOutput());
  }

  public static final void checkDynamicallyValid(final Program program) {
    checkDynamicallyValid(program, UNBOUNDED, UNBOUNDED);
  }

  public static final void checkDynamicallyValid(final Program program,
      final int maxNumberOfSteps, final int maxNumberOfLoopIterations) {
    final State state = new State();
    final Interpreter interpreter =
        new Interpreter(true, maxNumberOfSteps, maxNumberOfLoopIterations);

    final Value exitValue = interpreter.visit(program, state);
    final List<Value> output = state.getOutput();

    if (isUndefined(exitValue)) {
      throw InvalidProgramException.dynamicallyInvalid("undefined exit value");
    }
  }

  // ===============================================================================================

  public abstract static class Value {

    public final Type type;

    public Value(final Type type) {
      this.type = type;
    }

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(final Object other);

  }

  public static final class NumberValue extends Value {

    public static final NumberValue UNDEFINED = new NumberValue(-1);

    public final long value;

    public NumberValue(final long value) {
      super(AtomicType.INT);
      this.value = value;
    }

    @Override
    public final String toString() {
      if (this == UNDEFINED) {
        return "UNDEF";
      } else {
        return String.valueOf(this.value);
      }
    }

    @Override
    public final boolean equals(final Object other) {
      if (!(other instanceof NumberValue)) {
        return false;
      }

      if (this == UNDEFINED || other == UNDEFINED) {
        // undefined values are never equal
        return false;
      }

      return this.value == ((NumberValue) other).value;
    }

  }

  public static final class BooleanValue extends Value {

    public static final BooleanValue UNDEFINED = new BooleanValue(false);

    public static final BooleanValue TRUE = new BooleanValue(true);
    public static final BooleanValue FALSE = new BooleanValue(false);

    public final boolean value;

    public BooleanValue(final boolean value) {
      super(AtomicType.BOOLEAN);
      this.value = value;
    }

    @Override
    public final String toString() {
      if (this == UNDEFINED) {
        return "UNDEF";
      } else {
        return String.valueOf(this.value);
      }
    }

    @Override
    public final boolean equals(final Object other) {
      if (!(other instanceof BooleanValue)) {
        return false;
      }

      if (this == UNDEFINED || other == UNDEFINED) {
        // undefined values are never equal
        return false;
      }

      return this.value == ((BooleanValue) other).value;
    }

  }

  public static final class State {

    private final Map<Symbol, FunctionDeclaration> functions;

    private final Map<Symbol, Value> globalVariables;

    private final List<Map<Symbol, Value>> stack;
    private final List<Type> returnTypes;

    private final List<Value> output;

    public State() {
      this.functions = new HashMap<Symbol, FunctionDeclaration>();
      this.globalVariables = new HashMap<Symbol, Value>();
      this.stack = new ArrayList<Map<Symbol, Value>>();
      this.returnTypes = new ArrayList<Type>();
      this.output = new ArrayList<Value>();
    }

    public final void registerFunction(final FunctionDeclaration function) {
      assert (function.getSymbol() != null);
      this.functions.put(function.getSymbol(), function);
    }

    public final FunctionDeclaration getFunction(final Symbol symbol) {
      return this.functions.get(symbol);
    }

    public final void defineVariable(final Symbol symbol, final Value value) {
      if (symbol.isGlobal()) {
        this.globalVariables.put(symbol, value);
      } else {
        assert (!this.stack.isEmpty());
        this.stack.get(this.stack.size() - 1).put(symbol, value);
      }
    }

    public final Value readVariable(final Symbol symbol) {
      final Map<Symbol, Value> valueMap;
      {
        if (symbol.isGlobal()) {
          valueMap = this.globalVariables;
        } else {
          assert (!this.stack.isEmpty());
          valueMap = this.stack.get(this.stack.size() - 1);
        }
      }

      if (valueMap.containsKey(symbol)) {
        return valueMap.get(symbol);
      } else {
        assert (symbol.getType() == AtomicType.INT) : "only INT variables supported";
        return NumberValue.UNDEFINED;
      }
    }

    public final void enterFunction(final Map<Symbol, Value> argumentValues,
        final Type returnType) {
      this.stack.add(argumentValues);
      this.returnTypes.add(returnType);
    }

    public final void leaveFunction() {
      assert (!this.stack.isEmpty());
      assert (!this.returnTypes.isEmpty());

      this.stack.remove(this.stack.size() - 1);
      this.returnTypes.remove(this.returnTypes.size() - 1);
    }

    public final Type getReturnType() {
      assert (!this.returnTypes.isEmpty());
      return this.returnTypes.get(this.returnTypes.size() - 1);
    }

    public final void print(final Value value) {
      this.output.add(value);
    }

    public final List<Value> getOutput() {
      return Collections.unmodifiableList(this.output);
    }

  }

  // ===============================================================================================

  private static final NumberValue toNumber(final Value value) {
    if (value instanceof NumberValue) {
      return (NumberValue) value;
    }

    if (value instanceof BooleanValue) {
      if (value == BooleanValue.UNDEFINED) {
        return NumberValue.UNDEFINED;
      }

      final boolean booleanValue = ((BooleanValue) value).value;

      if (booleanValue) {
        return new NumberValue(1);
      } else {
        return new NumberValue(0);
      }
    }

    assert (false) : String.format(
        "cannot convert a '%s' to a 'NumberValue'", value.getClass().getSimpleName());
    return null;
  }

  private static final BooleanValue toBoolean(final Value value) {
    if (value instanceof BooleanValue) {
      return (BooleanValue) value;
    }

    if (value instanceof NumberValue) {
      if (value == NumberValue.UNDEFINED) {
        return BooleanValue.UNDEFINED;
      }

      final long numberValue = ((NumberValue) value).value;

      if (numberValue == 0) {
        return new BooleanValue(false);
      } else {
        return new BooleanValue(true);
      }
    }

    assert (false) : String.format(
        "cannot convert a '%s' to a 'BooleanValue'", value.getClass().getSimpleName());
    return null;
  }

  private static final boolean isTrue(final BooleanValue booleanValue) {
    if (booleanValue == BooleanValue.UNDEFINED) {
      return false;
    }

    return booleanValue.value;
  }

  private static final boolean isFalse(final BooleanValue booleanValue) {
    if (booleanValue == BooleanValue.UNDEFINED) {
      return false;
    }

    return !booleanValue.value;
  }

  private static final boolean isUndefined(final Value value) {
    assert ((value instanceof NumberValue) || (value instanceof BooleanValue));
    return (value == NumberValue.UNDEFINED) || (value == BooleanValue.UNDEFINED);
  }

  private static final Value getUndefined(final AtomicType type) {
    if (type == AtomicType.INT) {
      return NumberValue.UNDEFINED;
    } else {
      assert (type == AtomicType.BOOLEAN);
      return BooleanValue.UNDEFINED;
    }
  }

  private static final class Return extends RuntimeException {

    public final Value returnValue;

    public Return() {
      this(null);
    }

    public Return(final Value returnValue) {
      this.returnValue = returnValue;
    }

  }

  // ===============================================================================================

  private final int maxNumberOfSteps;
  private final int maxNumberOfLoopIterations;
  private final boolean abortOnUndefinedBehavior;

  private int numberOfSteps;

  private Interpreter(final boolean abortOnUndefinedBehavior, final int maxNumberOfSteps,
      final int maxNumberOfLoopIterations) {
    this.abortOnUndefinedBehavior = abortOnUndefinedBehavior;
    this.maxNumberOfSteps = maxNumberOfSteps;
    this.maxNumberOfLoopIterations = maxNumberOfLoopIterations;
  }

  private final void checkNumberOfSteps(final int numberOfSteps, final int maxNumberOfSteps) {
    if (maxNumberOfSteps != UNBOUNDED && numberOfSteps > maxNumberOfSteps) {
      throw new Timeout();
    }
  }

  private static final boolean isPowerOfTwo(final long value) {
    return (value > 1) && ((value & (value - 1)) == 0);
  }

  @Override
  public final Value visit(final Program program, final State state) {
    this.numberOfSteps = 0;

    FunctionDeclaration mainFunction = null;

    for (final Declaration globalDeclaration : program.getDeclarations()) {
      if (globalDeclaration instanceof FunctionDeclaration) {
        final FunctionDeclaration functionDeclaration = (FunctionDeclaration) globalDeclaration;

        state.registerFunction(functionDeclaration);

        if ("main".equals(functionDeclaration.getName().getName())) {
          mainFunction = functionDeclaration;
        }
      } else {
        assert (globalDeclaration instanceof VariableDeclaration);
        final VariableDeclaration variableDeclaration = (VariableDeclaration) globalDeclaration;
        visit(variableDeclaration, state);
      }
    }

    if (mainFunction != null) {
      final Type returnType;
      {
        assert (mainFunction.getSymbol().getType() instanceof FunctionType);
        returnType = ((FunctionType) mainFunction.getSymbol().getType()).getReturnType();
      }

      state.enterFunction(new HashMap<Symbol, Value>(), returnType);
      return visit(mainFunction, state);
    }

    return new NumberValue(0);
  }

  @Override
  public final Value visit(final VariableDeclaration variableDeclaration, final State state) {
    final Symbol symbol = variableDeclaration.getSymbol();

    // check for injected bug
    if (!Bugs.getInstance().isEnabled(Bug.MISSING_INIT_GLOBALS)) {
      if (symbol.isGlobal()) {
        // only global variables have a predefined value
        assert (variableDeclaration.getType() == AtomicType.INT) : "only INT variables supported";

        final NumberValue initialValue = new NumberValue(0);
        state.defineVariable(symbol, initialValue);

        return initialValue;
      }
    }

    return null;
  }

  @Override
  public final Value visit(final FunctionDeclaration functionDeclaration, final State state) {
    try {
      final Block body = functionDeclaration.getBody();
      visit(body, state);
    } catch (final Return reachedReturn) {
      return reachedReturn.returnValue;
    } finally {
      state.leaveFunction();
    }

    final Type returnType = functionDeclaration.getReturnType().getType();

    if (returnType == AtomicType.VOID) {
      return null;
    } else {
      assert (returnType == AtomicType.INT);
      return NumberValue.UNDEFINED;
    }
  }

  @Override
  public final Value visit(final Block block, final State state) {
    for (final Statement statement : block.getStatements()) {
      statement.accept(this, state);
    }

    return null;
  }

  @Override
  public final Value visit(final TypeName typeName, final State state) {
    // intentionally left blank
    return null;
  }

  @Override
  public final Value visit(final AssignStatement assignStatement, final State state) {
    checkNumberOfSteps(++this.numberOfSteps, this.maxNumberOfSteps);

    final Expression rightHandSide = assignStatement.getRightHandSide();
    final Value value = rightHandSide.accept(this, state);

    final Symbol variable = assignStatement.getLeftHandSide().getSymbol();
    state.defineVariable(variable, value);

    return value;
  }

  @Override
  public final Value visit(final FunctionCallStatement functionCallStatement, final State state) {
    checkNumberOfSteps(++this.numberOfSteps, this.maxNumberOfSteps);

    final FunctionCall functionCall = functionCallStatement.getFunctionCall();
    return visit(functionCall, state);
  }

  @Override
  public final Value visit(final IfStatement ifStatement, final State state) {
    checkNumberOfSteps(++this.numberOfSteps, this.maxNumberOfSteps);

    final Expression condition = ifStatement.getCondition();
    final BooleanValue conditionValue = toBoolean(condition.accept(this, state));

    if (this.abortOnUndefinedBehavior && isUndefined(conditionValue)) {
      throw InvalidProgramException.dynamicallyInvalid(
          condition.getPosition(), "undefined control flow");
    }

    if (isTrue(conditionValue)) {
      final Block thenBlock = ifStatement.getThenBlock();
      visit(thenBlock, state);
    } else {
      if (ifStatement.hasElseBlock()) {
        final Block elseBlock = ifStatement.getElseBlock();
        visit(elseBlock, state);
      }
    }

    return null;
  }

  @Override
  public final Value visit(final WhileLoop whileLoop, final State state) {
    int numberOfIterations = 0;

    while (true) {
      checkNumberOfSteps(++this.numberOfSteps, this.maxNumberOfSteps);

      final Expression condition = whileLoop.getCondition();
      final BooleanValue conditionValue = toBoolean(condition.accept(this, state));

      if (this.abortOnUndefinedBehavior && isUndefined(conditionValue)) {
        throw InvalidProgramException.dynamicallyInvalid(
            condition.getPosition(), "undefined control flow");
      }

      if (isTrue(conditionValue)) {
        checkNumberOfSteps(++numberOfIterations, this.maxNumberOfLoopIterations);

        final Block body = whileLoop.getBody();
        visit(body, state);
      } else {
        break;
      }
    }

    return null;
  }

  @Override
  public final Value visit(final ReturnStatement returnStatement, final State state) {
    checkNumberOfSteps(++this.numberOfSteps, this.maxNumberOfSteps);

    if (returnStatement.hasReturnValue()) {
      final Value returnValue = returnStatement.getReturnValue().accept(this, state);

      assert (state.getReturnType() == AtomicType.INT);
      throw new Return(toNumber(returnValue));
    } else {
      assert (state.getReturnType() == AtomicType.VOID);
      throw new Return();
    }
  }

  @Override
  public final Value visit(final DeclarationStatement declarationStatement, final State state) {
    final Declaration declaration = declarationStatement.getDeclaration();
    return declaration.accept(this, state);
  }

  @Override
  public final Value visit(final Identifier identifier, final State state) {
    final Value value = state.readVariable(identifier.getSymbol());
    return value;
  }

  @Override
  public final Value visit(final Literal literal, final State state) {
    final String value = literal.getToken().string;
    return new NumberValue(Long.parseLong(value));
  }

  @Override
  public final Value visit(final BinaryExpression binaryExpression, final State state) {
    final BinaryExpression.Operator operator = binaryExpression.getOperator();
    final Expression leftHandSide = binaryExpression.getLeftHandSide();
    final Expression rightHandSide = binaryExpression.getRightHandSide();

    // check for injected bug
    final boolean noShortcutOr = Bugs.getInstance().isEnabled(Bug.NO_SHORTCUT_OR);
    final boolean noShortcutAnd = Bugs.getInstance().isEnabled(Bug.NO_SHORTCUT_AND);

    // OR and AND need special treatment due to shortcut evaluation
    if (operator == BinaryExpression.Operator.OR && !noShortcutOr) {
      final BooleanValue leftValue = toBoolean(leftHandSide.accept(this, state));

      if (isUndefined(leftValue)) {
        return BooleanValue.UNDEFINED;
      }

      if (isTrue(leftValue)) {
        return BooleanValue.TRUE;
      } else {
        final BooleanValue rightValue = toBoolean(rightHandSide.accept(this, state));
        return rightValue;
      }
    } else if (operator == BinaryExpression.Operator.AND && !noShortcutAnd) {
      final BooleanValue leftValue = toBoolean(leftHandSide.accept(this, state));

      if (isUndefined(leftValue)) {
        return BooleanValue.UNDEFINED;
      }

      if (isFalse(leftValue)) {
        return BooleanValue.FALSE;
      } else {
        final BooleanValue rightValue = toBoolean(rightHandSide.accept(this, state));
        return rightValue;
      }
    } else {
      final Value leftValue = leftHandSide.accept(this, state);
      final Value rightValue = rightHandSide.accept(this, state);

      if (isUndefined(leftValue) || isUndefined(rightValue)) {
        // NOTE: this is only relevant if the 'no_shortcut_or' bug is enabled
        if (operator == BinaryExpression.Operator.OR
            && (isTrue(toBoolean(leftValue)) || isTrue(toBoolean(rightValue)))) {
          return BooleanValue.TRUE;
        }

        final Type resultType = binaryExpression.getOperator().resultType;

        assert (resultType instanceof AtomicType);
        return getUndefined((AtomicType) resultType);
      }

      switch (operator) {
        case OR: {
          return new BooleanValue(toBoolean(leftValue).value || toBoolean(rightValue).value);
        }
        case AND: {
          return new BooleanValue(toBoolean(leftValue).value && toBoolean(rightValue).value);
        }
        case EQUALS: {
          return new BooleanValue(toNumber(leftValue).value == toNumber(rightValue).value);
        }
        case LESS_THAN: {
          return new BooleanValue(toNumber(leftValue).value < toNumber(rightValue).value);
        }
        case LESS_EQUALS: {
          return new BooleanValue(toNumber(leftValue).value <= toNumber(rightValue).value);
        }
        case GREATER_THAN: {
          return new BooleanValue(toNumber(leftValue).value > toNumber(rightValue).value);
        }
        case GREATER_EQUALS: {
          return new BooleanValue(toNumber(leftValue).value >= toNumber(rightValue).value);
        }
        case NOT_EQUALS: {
          return new BooleanValue(toNumber(leftValue).value != toNumber(rightValue).value);
        }
        case ADD: {
          return new NumberValue(toNumber(leftValue).value + toNumber(rightValue).value);
        }
        case SUB: {
          return new NumberValue(toNumber(leftValue).value - toNumber(rightValue).value);
        }
        case MUL: {
          final NumberValue rightNumber = toNumber(rightValue);

          // check for injected bug
          if (Bugs.getInstance().isEnabled(Bug.WRONG_SHIFT_MUL)
              && isPowerOfTwo(rightNumber.value)) {
            return rightNumber;
          }

          return new NumberValue(toNumber(leftValue).value * rightNumber.value);
        }
        case DIV: {
          final NumberValue divisor = toNumber(rightValue);

          // check for injected bug
          if (!Bugs.getInstance().isEnabled(Bug.DIV_BY_ZERO)) {
            if (divisor.value == 0) {
              return NumberValue.UNDEFINED;
            }
          }

          return new NumberValue(toNumber(leftValue).value / divisor.value);
        }
        default: {
          assert (false) : "unknown binary operator: " + operator;
          return null;
        }
      }
    }
  }

  @Override
  public final Value visit(final FunctionCall functionCall, final State state) {
    final Symbol calleeSymbol = functionCall.getCallee().getSymbol();
    final List<Expression> arguments = functionCall.getArguments();

    if (calleeSymbol == Symbol.PRINT) {
      assert (arguments.size() == 1);
      final Expression argument = arguments.get(0);
      final Value argumentValue = argument.accept(this, state);

      if (this.abortOnUndefinedBehavior && isUndefined(argumentValue)) {
        throw InvalidProgramException.dynamicallyInvalid(
            argument.getPosition(), "undefined output");
      }

      state.print(argumentValue);

      return null;
    } else {
      assert (calleeSymbol.getDeclaration() instanceof FunctionDeclaration);
      final FunctionDeclaration callee = (FunctionDeclaration) calleeSymbol.getDeclaration();

      assert (calleeSymbol.getType() instanceof FunctionType);
      final FunctionType calleeType = (FunctionType) calleeSymbol.getType();

      final Map<Symbol, Value> argumentValues = new HashMap<>();

      final List<VariableDeclaration> parameters = callee.getParameters();

      assert (arguments.size() == parameters.size());

      for (int index = 0; index < arguments.size(); ++index) {
        final Expression argument = arguments.get(index);
        final Value argumentValue = argument.accept(this, state);

        final VariableDeclaration parameter = parameters.get(index);
        final Symbol parameterSymbol = parameter.getSymbol();

        argumentValues.put(parameterSymbol, argumentValue);
      }

      state.enterFunction(argumentValues, calleeType.getReturnType());
      return visit(callee, state);
    }
  }

}
