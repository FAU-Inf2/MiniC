package i2.act.examples.minic.frontend.semantics;

import i2.act.examples.minic.bugs.Bug;
import i2.act.examples.minic.bugs.Bugs;
import i2.act.examples.minic.errors.InvalidProgramException;
import i2.act.examples.minic.frontend.ast.*;
import i2.act.examples.minic.frontend.ast.visitors.BaseASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.lexer.Token;
import i2.act.examples.minic.frontend.semantics.symbols.Symbol;
import i2.act.examples.minic.frontend.semantics.symbols.SymbolTable;
import i2.act.examples.minic.frontend.semantics.types.AtomicType;
import i2.act.examples.minic.frontend.semantics.types.FunctionType;
import i2.act.examples.minic.frontend.semantics.types.Type;

import java.util.ArrayList;
import java.util.List;

public final class SemanticAnalysis extends BaseASTVisitor<SymbolTable, Type> {

  public static final void analyze(final Program program) {
    final SymbolTable symbolTable = new SymbolTable();
    symbolTable.enterScope();
    symbolTable.declare(Symbol.PRINT, SourcePosition.UNKNOWN);

    final SemanticAnalysis analysis = new SemanticAnalysis();
    analysis.visit(program, symbolTable);
  }

  // ===============================================================================================

  private SemanticAnalysis() {
    // intentionally left blank
  }

  private Type expectedReturnType;

  @Override
  public final Type visit(final Program program, final SymbolTable symbolTable) {
    return super.visit(program, symbolTable);
  }

  @Override
  public final Type visit(final VariableDeclaration variableDeclaration,
      final SymbolTable symbolTable) {
    final Identifier identifier = variableDeclaration.getName();
    final String variableName = identifier.getToken().string;

    final TypeName typeName = variableDeclaration.getTypeName();
    final Type type = typeName.accept(this, symbolTable);

    if (!type.isVariableType()) {
      throw InvalidProgramException.semanticallyInvalid(variableDeclaration.getPosition(),
          String.format("%s is not a valid type for variables", type));
    }

    final boolean inGlobalScope = symbolTable.numberOfScopes() == 1;
    final Symbol symbol =
        new Symbol(variableName, type, inGlobalScope, variableDeclaration);

    symbolTable.declare(symbol, variableDeclaration.getPosition());
    identifier.setSymbol(symbol);

    return type;
  }

  @Override
  public final Type visit(final FunctionDeclaration functionDeclaration,
      final SymbolTable symbolTable) {
    final TypeName returnTypeName = functionDeclaration.getReturnType();
    final Type returnType = returnTypeName.accept(this, symbolTable);

    final Identifier identifier = functionDeclaration.getName();
    final String functionName = identifier.getToken().string;

    final List<Type> parameterTypes = new ArrayList<>();
    final FunctionType functionType = new FunctionType(returnType, parameterTypes);

    final boolean inGlobalScope = symbolTable.numberOfScopes() == 1;
    final Symbol symbol =
        new Symbol(functionName, functionType, inGlobalScope, functionDeclaration);

    symbolTable.declare(symbol, functionDeclaration.getPosition());
    identifier.setSymbol(symbol);

    symbolTable.enterScope();

    final List<VariableDeclaration> paramters = functionDeclaration.getParameters();

    for (final VariableDeclaration parameter : paramters) {
      final Type parameterType = parameter.accept(this, symbolTable);
      parameterTypes.add(parameterType);
    }

    final Type oldExpectedReturnType = this.expectedReturnType;
    this.expectedReturnType = returnType;

    final Block body = functionDeclaration.getBody();
    body.accept(this, symbolTable);

    this.expectedReturnType = oldExpectedReturnType;

    symbolTable.leaveScope();

    return functionType;
  }

  @Override
  public final Type visit(final Block block, final SymbolTable symbolTable) {
    symbolTable.enterScope();

    final List<Statement> statements = block.getStatements();
    for (final Statement statement : statements) {
      statement.accept(this, symbolTable);
    }

    symbolTable.leaveScope();
    return null;
  }

  @Override
  public final Type visit(final TypeName typeName, final SymbolTable symbolTable) {
    final Token typeNameToken = typeName.getTypeNameToken();

    switch (typeNameToken.kind) {
      case TK_INT: {
        return AtomicType.INT;
      }
      case TK_VOID: {
        return AtomicType.VOID;
      }
      default: {
        throw new RuntimeException(String.format("unknown type name: '%s'", typeNameToken.kind));
      }
    }
  }

  @Override
  public final Type visit(final AssignStatement assignStatement, final SymbolTable symbolTable) {
    final Identifier leftHandSide = assignStatement.getLeftHandSide();
    final Type typeLeftHandSide = leftHandSide.accept(this, symbolTable);

    final Expression rightHandSide = assignStatement.getRightHandSide();
    final Type typeRightHandSide = rightHandSide.accept(this, symbolTable);

    if (!typeRightHandSide.assignableTo(typeLeftHandSide)) {
      throw InvalidProgramException.semanticallyInvalid(assignStatement.getPosition(),
          String.format("%s cannot be assigned to %s", typeRightHandSide, typeLeftHandSide));
    }

    return typeLeftHandSide;
  }

  @Override
  public final Type visit(final FunctionCallStatement functionCallStatement,
      final SymbolTable symbolTable) {
    final FunctionCall functionCall = functionCallStatement.getFunctionCall();
    return functionCall.accept(this, symbolTable);
  }

  @Override
  public final Type visit(final IfStatement ifStatement, final SymbolTable symbolTable) {
    final Expression condition = ifStatement.getCondition();
    final Type typeCondition = condition.accept(this, symbolTable);

    if (!typeCondition.assignableTo(AtomicType.BOOLEAN)) {
      throw InvalidProgramException.semanticallyInvalid(condition.getPosition(),
          String.format("if condition must be of type %s", AtomicType.BOOLEAN));
    }

    final Block thenBlock = ifStatement.getThenBlock();
    thenBlock.accept(this, symbolTable);

    if (ifStatement.hasElseBlock()) {
      final Block elseBlock = ifStatement.getElseBlock();
      elseBlock.accept(this, symbolTable);
    }

    return null;
  }

  @Override
  public final Type visit(final WhileLoop whileLoop, final SymbolTable symbolTable) {
    final Expression condition = whileLoop.getCondition();
    final Type typeCondition = condition.accept(this, symbolTable);

    if (!typeCondition.assignableTo(AtomicType.BOOLEAN)) {
      throw InvalidProgramException.semanticallyInvalid(condition.getPosition(),
          String.format("while condition must be of type %s", AtomicType.BOOLEAN));
    }

    final Block body = whileLoop.getBody();
    body.accept(this, symbolTable);

    return null;
  }

  @Override
  public final Type visit(final ReturnStatement returnStatement, final SymbolTable symbolTable) {
    assert (this.expectedReturnType != null);

    if (returnStatement.hasReturnValue()) {
      if (this.expectedReturnType == AtomicType.VOID) {
        throw InvalidProgramException.semanticallyInvalid(returnStatement.getPosition(),
            String.format("cannot return a value from a %s function", AtomicType.VOID));
      }

      final Expression returnValue = returnStatement.getReturnValue();
      final Type returnType = returnValue.accept(this, symbolTable);

      if (!returnType.assignableTo(this.expectedReturnType)) {
        throw InvalidProgramException.semanticallyInvalid(returnStatement.getPosition(),
            String.format("%s cannot be assigned to %s", returnType, this.expectedReturnType));
      }

      return returnType;
    } else {
      if (this.expectedReturnType != AtomicType.VOID) {
        throw InvalidProgramException.semanticallyInvalid(returnStatement.getPosition(),
            String.format("has to return a value of type %s", this.expectedReturnType));
      }

      return AtomicType.VOID;
    }
  }

  @Override
  public final Type visit(final DeclarationStatement declarationStatement,
      final SymbolTable symbolTable) {
    final Declaration declaration = declarationStatement.getDeclaration();
    return declaration.accept(this, symbolTable);
  }

  @Override
  public final Type visit(final Identifier identifier, final SymbolTable symbolTable) {
    final String name = identifier.getToken().string;
    final Symbol symbol = symbolTable.get(name, identifier.getPosition());

    identifier.setSymbol(symbol);

    return symbol.getType();
  }

  @Override
  public final Type visit(final Literal literal, final SymbolTable symbolTable) {
    final String value = literal.getToken().string;

    try {
      final int intValue = Integer.parseInt(value);
      return AtomicType.INT;
    } catch (final Exception exception) {
      throw new RuntimeException(String.format("unknown literal type: '%s'", value));
    }
  }

  @Override
  public final Type visit(final BinaryExpression binaryExpression, final SymbolTable symbolTable) {
    final BinaryExpression.Operator operator = binaryExpression.getOperator();

    final Expression leftHandSide = binaryExpression.getLeftHandSide();
    final Type typeLeftHandSide = leftHandSide.accept(this, symbolTable);

    if (!typeLeftHandSide.assignableTo(operator.sourceType)) {
      throw InvalidProgramException.semanticallyInvalid(leftHandSide.getPosition(),
          String.format("%s cannot be assigned to %s", typeLeftHandSide, operator.sourceType));
    }

    final Expression rightHandSide = binaryExpression.getRightHandSide();
    final Type typeRightHandSide = rightHandSide.accept(this, symbolTable);

    if (!typeRightHandSide.assignableTo(operator.sourceType)) {
      throw InvalidProgramException.semanticallyInvalid(rightHandSide.getPosition(),
          String.format("%s cannot be assigned to %s", typeRightHandSide, operator.sourceType));
    }

    return operator.resultType;
  }

  @Override
  public final Type visit(final FunctionCall functionCall, final SymbolTable symbolTable) {
    final Identifier callee = functionCall.getCallee();
    final Type calleeType = callee.accept(this, symbolTable);

    // check for injected bug
    {
      if (Bugs.getInstance().isEnabled(Bug.MISSING_SYMBOL_CALLEE)) {
        callee.setSymbol(null);
      }
    }

    if (!(calleeType instanceof FunctionType)) {
      throw InvalidProgramException.semanticallyInvalid(functionCall.getPosition(),
          String.format("%s of type %s is not a function",
              callee.getToken().string, calleeType));
    }

    final FunctionType functionType = (FunctionType) calleeType;

    final List<Expression> arguments = functionCall.getArguments();

    if (functionType.getNumberOfParameters() != arguments.size()) {
      throw InvalidProgramException.semanticallyInvalid(functionCall.getPosition(),
          String.format("expected %d arguments, but found %d",
              functionType.getNumberOfParameters(), arguments.size()));
    }

    final List<Type> parameterTypes = functionType.getParameterTypes();

    for (int index = 0; index < parameterTypes.size(); ++index) {
      final Expression argument = arguments.get(index);
      final Type argumentType = argument.accept(this, symbolTable);
      final Type parameterType = parameterTypes.get(index);

      if (!argumentType.assignableTo(parameterType)) {
        throw InvalidProgramException.semanticallyInvalid(argument.getPosition(),
            String.format("%s cannot be assigned to %s", argumentType, parameterType));
      }
    }

    return functionType.getReturnType();
  }

}
