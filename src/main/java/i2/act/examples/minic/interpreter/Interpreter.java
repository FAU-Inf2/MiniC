package i2.act.examples.minic.interpreter;

import i2.act.examples.minic.frontend.ast.*;
import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.semantics.symbols.Symbol;
import i2.act.examples.minic.frontend.semantics.types.AtomicType;
import i2.act.examples.minic.frontend.semantics.types.FunctionType;
import i2.act.examples.minic.frontend.semantics.types.Type;
import i2.act.util.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Interpreter implements ASTVisitor<Interpreter.State, Interpreter.Value> {

  public static final Pair<Value, List<Value>> interpret(final Program program) {
    final State state = new State();
    final Interpreter interpreter = new Interpreter();

    final Value exitValue = interpreter.visit(program, state);

    return new Pair<Value, List<Value>>(exitValue, state.getOutput());
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

    public static final NumberValue UNDEFINED = new NumberValue(null);

    public final BigInteger value;

    public NumberValue(final BigInteger value) {
      super(AtomicType.INT);
      this.value = value;
    }

    @Override
    public final String toString() {
      if (this == UNDEFINED) {
        return "UNDEF";
      } else {
        return this.value.toString();
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

      return this.value.equals(((NumberValue) other).value);
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

    private final List<Value> output;

    public State() {
      this.functions = new HashMap<Symbol, FunctionDeclaration>();
      this.globalVariables = new HashMap<Symbol, Value>();
      this.stack = new ArrayList<Map<Symbol, Value>>();
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

    public final void enterFunction(final Map<Symbol, Value> argumentValues) {
      this.stack.add(argumentValues);
    }

    public final void leaveFunction() {
      assert (!this.stack.isEmpty());
      this.stack.remove(this.stack.size() - 1);
    }

    public final void print(final Value value) {
      this.output.add(value);
    }

    public final List<Value> getOutput() {
      return Collections.unmodifiableList(this.output);
    }

  }

  // ===============================================================================================

  private final NumberValue toNumber(final Value value) {
    if (value instanceof NumberValue) {
      return (NumberValue) value;
    }

    if (value instanceof BooleanValue) {
      if (value == BooleanValue.UNDEFINED) {
        return NumberValue.UNDEFINED;
      }

      final boolean booleanValue = ((BooleanValue) value).value;

      if (booleanValue) {
        return new NumberValue(BigInteger.ONE);
      } else {
        return new NumberValue(BigInteger.ZERO);
      }
    }

    assert (false) : String.format(
        "cannot convert a '%s' to a 'NumberValue'", value.getClass().getSimpleName());
    return null;
  }

  private final BooleanValue toBoolean(final Value value) {
    if (value instanceof BooleanValue) {
      return (BooleanValue) value;
    }

    if (value instanceof NumberValue) {
      if (value == NumberValue.UNDEFINED) {
        return BooleanValue.UNDEFINED;
      }

      final BigInteger numberValue = ((NumberValue) value).value;

      if (BigInteger.ZERO.equals(numberValue)) {
        return new BooleanValue(false);
      } else {
        return new BooleanValue(true);
      }
    }

    assert (false) : String.format(
        "cannot convert a '%s' to a 'BooleanValue'", value.getClass().getSimpleName());
    return null;
  }

  private final boolean isTrue(final BooleanValue booleanValue) {
    if (booleanValue == BooleanValue.UNDEFINED) {
      // TODO handle undefined boolean
    }

    return booleanValue.value;
  }

  private final boolean isFalse(final BooleanValue booleanValue) {
    if (booleanValue == BooleanValue.UNDEFINED) {
      // TODO handle undefined boolean
    }

    return !booleanValue.value;
  }

  private final boolean isUndefined(final Value value) {
    assert ((value instanceof NumberValue) || (value instanceof BooleanValue));
    return (value == NumberValue.UNDEFINED) || (value == BooleanValue.UNDEFINED);
  }

  private final Value getUndefined(final AtomicType type) {
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

  @Override
  public final Value visit(final Program program, final State state) {
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
      state.enterFunction(new HashMap<Symbol, Value>());
      return visit(mainFunction, state);
    }

    return new NumberValue(BigInteger.ZERO);
  }

  @Override
  public final Value visit(final VariableDeclaration variableDeclaration, final State state) {
    final Symbol symbol = variableDeclaration.getSymbol();

    if (symbol.isGlobal()) {
      // only global variables have a predefined value
      assert (variableDeclaration.getType() == AtomicType.INT) : "only INT variables supported";

      final NumberValue initialValue = new NumberValue(BigInteger.ZERO);
      state.defineVariable(symbol, initialValue);

      return initialValue;
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
    final Expression rightHandSide = assignStatement.getRightHandSide();
    final Value value = rightHandSide.accept(this, state);

    final Symbol variable = assignStatement.getLeftHandSide().getSymbol();
    state.defineVariable(variable, value);

    return value;
  }

  @Override
  public final Value visit(final IfStatement ifStatement, final State state) {
    final Expression condition = ifStatement.getCondition();
    final BooleanValue conditionValue = toBoolean(condition.accept(this, state));

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
  public final Value visit(final ReturnStatement returnStatement, final State state) {
    if (returnStatement.hasReturnValue()) {
      final Value returnValue = returnStatement.getReturnValue().accept(this, state);
      throw new Return(returnValue);
    } else {
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
    return new NumberValue(new BigInteger(value));
  }

  @Override
  public final Value visit(final BinaryExpression binaryExpression, final State state) {
    final BinaryExpression.Operator operator = binaryExpression.getOperator();
    final Expression leftHandSide = binaryExpression.getLeftHandSide();
    final Expression rightHandSide = binaryExpression.getRightHandSide();

    // OR and AND need special treatment due to shortcut evaluation
    if (operator == BinaryExpression.Operator.OR) {
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
    } else if (operator == BinaryExpression.Operator.AND) {
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
        final Type resultType = binaryExpression.getOperator().resultType;

        assert (resultType instanceof AtomicType);
        return getUndefined((AtomicType) resultType);
      }

      switch (operator) {
        case EQUALS: {
          final int compare = toNumber(leftValue).value.compareTo(toNumber(rightValue).value);
          return new BooleanValue(compare == 0);
        }
        case LESS_THAN: {
          final int compare = toNumber(leftValue).value.compareTo(toNumber(rightValue).value);
          return new BooleanValue(compare < 0);
        }
        case LESS_EQUALS: {
          final int compare = toNumber(leftValue).value.compareTo(toNumber(rightValue).value);
          return new BooleanValue(compare < 0 || compare == 0);
        }
        case GREATER_THAN: {
          final int compare = toNumber(leftValue).value.compareTo(toNumber(rightValue).value);
          return new BooleanValue(compare > 0);
        }
        case GREATER_EQUALS: {
          final int compare = toNumber(leftValue).value.compareTo(toNumber(rightValue).value);
          return new BooleanValue(compare > 0 || compare == 0);
        }
        case NOT_EQUALS: {
          final int compare = toNumber(leftValue).value.compareTo(toNumber(rightValue).value);
          return new BooleanValue(compare != 0);
        }
        case ADD: {
          return new NumberValue(toNumber(leftValue).value.add(toNumber(rightValue).value));
        }
        case SUB: {
          return new NumberValue(toNumber(leftValue).value.subtract(toNumber(rightValue).value));
        }
        case MUL: {
          return new NumberValue(toNumber(leftValue).value.multiply(toNumber(rightValue).value));
        }
        case DIV: {
          return new NumberValue(toNumber(leftValue).value.divide(toNumber(rightValue).value));
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

    assert (calleeSymbol.getDeclaration() instanceof FunctionDeclaration);
    final FunctionDeclaration callee = (FunctionDeclaration) calleeSymbol.getDeclaration();

    assert (calleeSymbol.getType() instanceof FunctionType);
    final FunctionType calleeType = (FunctionType) calleeSymbol.getType();

    final Map<Symbol, Value> argumentValues = new HashMap<>();

    final List<Expression> arguments = functionCall.getArguments();
    final List<VariableDeclaration> parameters = callee.getParameters();

    assert (arguments.size() == parameters.size());

    for (int index = 0; index < arguments.size(); ++index) {
      final Expression argument = arguments.get(index);
      final Value argumentValue = argument.accept(this, state);

      final VariableDeclaration parameter = parameters.get(index);
      final Symbol parameterSymbol = parameter.getSymbol();

      argumentValues.put(parameterSymbol, argumentValue);
    }

    state.enterFunction(argumentValues);
    return visit(callee, state);
  }

}
