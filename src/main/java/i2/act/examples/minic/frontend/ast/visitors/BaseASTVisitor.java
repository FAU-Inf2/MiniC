package i2.act.examples.minic.frontend.ast.visitors;

import i2.act.examples.minic.frontend.ast.*;

public abstract class BaseASTVisitor<P, R> implements ASTVisitor<P, R>  {

  protected R prolog(final ASTNode node, final P parameter) {
    // intentionally left blank
    return null;
  }

  protected R epilog(final ASTNode node, final P parameter) {
    // intentionally left blank
    return null;
  }

  protected R beforeChild(final ASTNode parent, final ASTNode child, final P parameter) {
    // intentionally left blank
    return null;
  }

  protected R afterChild(final ASTNode parent, final ASTNode child, final P parameter) {
    // intentionally left blank
    return null;
  }

  protected final R visitChild(final ASTNode parent, final ASTNode child, final P parameter) {
    beforeChild(parent, child, parameter);
    final R returnValue = child.accept(this, parameter);
    afterChild(parent, child, parameter);

    return returnValue;
  }

  @Override
  public R visit(final Program program, final P parameter) {
    prolog(program, parameter);

    for (final Declaration declaration : program.getDeclarations()) {
      visitChild(program, declaration, parameter);
    }

    return epilog(program, parameter);
  }

  @Override
  public R visit(final VariableDeclaration variableDeclaration, final P parameter) {
    prolog(variableDeclaration, parameter);

    final TypeName typeName = variableDeclaration.getTypeName();
    visitChild(variableDeclaration, typeName, parameter);

    final Identifier name = variableDeclaration.getName();
    visitChild(variableDeclaration, name, parameter);

    return epilog(variableDeclaration, parameter);
  }

  @Override
  public R visit(final FunctionDeclaration functionDeclaration, final P parameter) {
    prolog(functionDeclaration, parameter);

    final TypeName returnType = functionDeclaration.getReturnType();
    visitChild(functionDeclaration, returnType, parameter);

    final Identifier name = functionDeclaration.getName();
    visitChild(functionDeclaration, name, parameter);

    for (final VariableDeclaration parameterDeclaration : functionDeclaration.getParameters()) {
      visitChild(functionDeclaration, parameterDeclaration, parameter);
    }

    final Block body = functionDeclaration.getBody();
    visitChild(functionDeclaration, body, parameter);

    return epilog(functionDeclaration, parameter);
  }

  @Override
  public R visit(final Block block, final P parameter) {
    prolog(block, parameter);

    for (final Statement statement : block.getStatements()) {
      visitChild(block, statement, parameter);
    }

    return epilog(block, parameter);
  }

  @Override
  public R visit(final TypeName typeName, final P parameter) {
    prolog(typeName, parameter);
    return epilog(typeName, parameter);
  }

  @Override
  public R visit(final AssignStatement assignStatement, final P parameter) {
    prolog(assignStatement, parameter);

    final Identifier leftHandSide = assignStatement.getLeftHandSide();
    visitChild(assignStatement, leftHandSide, parameter);

    final Expression rightHandSide = assignStatement.getRightHandSide();
    visitChild(assignStatement, rightHandSide, parameter);

    return epilog(assignStatement, parameter);
  }

  @Override
  public R visit(final IfStatement ifStatement, final P parameter) {
    prolog(ifStatement, parameter);

    final Expression condition = ifStatement.getCondition();
    visitChild(ifStatement, condition, parameter);

    final Block thenBlock = ifStatement.getThenBlock();
    visitChild(ifStatement, thenBlock, parameter);

    if (ifStatement.hasElseBlock()) {
      final Block elseBlock = ifStatement.getElseBlock();
      visitChild(ifStatement, elseBlock, parameter);
    }

    return epilog(ifStatement, parameter);
  }

  @Override
  public R visit(final WhileLoop whileLoop, final P parameter) {
    prolog(whileLoop, parameter);

    final Expression condition = whileLoop.getCondition();
    visitChild(whileLoop, condition, parameter);

    final Block body = whileLoop.getBody();
    visitChild(whileLoop, body, parameter);

    return epilog(whileLoop, parameter);
  }

  @Override
  public R visit(final ReturnStatement returnStatement, final P parameter) {
    prolog(returnStatement, parameter);

    if (returnStatement.hasReturnValue()) {
      final Expression returnValue = returnStatement.getReturnValue();
      visitChild(returnStatement, returnValue, parameter);
    }

    return epilog(returnStatement, parameter);
  }

  @Override
  public R visit(final DeclarationStatement declarationStatement, final P parameter) {
    prolog(declarationStatement, parameter);

    final Declaration declaration = declarationStatement.getDeclaration();
    visitChild(declarationStatement, declaration, parameter);

    return epilog(declarationStatement, parameter);
  }

  @Override
  public R visit(final Identifier identifier, final P parameter) {
    prolog(identifier, parameter);
    return epilog(identifier, parameter);
  }

  @Override
  public R visit(final Literal literal, final P parameter) {
    prolog(literal, parameter);
    return epilog(literal, parameter);
  }

  @Override
  public R visit(final BinaryExpression binaryExpression, final P parameter) {
    prolog(binaryExpression, parameter);

    final Expression leftHandSide = binaryExpression.getLeftHandSide();
    visitChild(binaryExpression, leftHandSide, parameter);

    final Expression rightHandSide = binaryExpression.getRightHandSide();
    visitChild(binaryExpression, rightHandSide, parameter);

    return epilog(binaryExpression, parameter);
  }

  @Override
  public R visit(final FunctionCall functionCall, final P parameter) {
    prolog(functionCall, parameter);

    final Identifier callee = functionCall.getCallee();
    visitChild(functionCall, callee, parameter);

    for (final Expression argument : functionCall.getArguments()) {
      visitChild(functionCall, argument, parameter);
    }

    return epilog(functionCall, parameter);
  }

}

