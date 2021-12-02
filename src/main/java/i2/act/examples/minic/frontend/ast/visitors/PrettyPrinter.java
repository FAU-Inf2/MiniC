package i2.act.examples.minic.frontend.ast.visitors;

import i2.act.examples.minic.frontend.ast.*;
import i2.act.util.SafeWriter;

public final class PrettyPrinter implements ASTVisitor<SafeWriter, Void> {

  public static final void prettyPrint(final Program program, final SafeWriter writer) {
    final PrettyPrinter prettyPrinter = new PrettyPrinter();
    prettyPrinter.visit(program, writer);
  }

  // ===============================================================================================

  private int currentIndentation;
  private boolean globalDeclaration;

  private PrettyPrinter() {
    this.currentIndentation = 0;
  }

  private final void writeIndentation(final SafeWriter writer) {
    for (int count = 0; count < this.currentIndentation; ++count) {
      writer.write(" ");
    }
  }

  private final boolean parenthesesNeeded(final BinaryExpression parent, final Expression child) {
    if (child instanceof BinaryExpression) {
      final int parentPrecedence = parent.getOperator().precedence;
      final int childPrecedence = ((BinaryExpression) child).getOperator().precedence;

      return (parentPrecedence >= childPrecedence);
    } else {
      assert ((child instanceof Identifier)
          || (child instanceof Literal)
          || (child instanceof FunctionCall));

      return false;
    }
  }

  @Override
  public final Void visit(final Program program, final SafeWriter writer) {
    boolean first = true;

    for (final Declaration declaration : program.getDeclarations()) {
      if (first) {
        first = false;
      } else {
        writer.write("\n");
      }

      this.globalDeclaration = true;
      declaration.accept(this, writer);
    }

    return null;
  }

  @Override
  public final Void visit(final VariableDeclaration variableDeclaration, final SafeWriter writer) {
    final TypeName typeName = variableDeclaration.getTypeName();
    typeName.accept(this, writer);

    writer.write(" ");

    final Identifier name = variableDeclaration.getName();
    name.accept(this, writer);

    if (this.globalDeclaration) {
      writer.write(";");
    }

    return null;
  }

  @Override
  public final Void visit(final FunctionDeclaration functionDeclaration, final SafeWriter writer) {
    final boolean wasGlobalDeclaration = this.globalDeclaration;
    this.globalDeclaration = false;

    final TypeName returnType = functionDeclaration.getReturnType();
    returnType.accept(this, writer);

    writer.write(" ");

    final Identifier name = functionDeclaration.getName();
    name.accept(this, writer);

    writer.write("(");

    boolean first = true;
    for (final VariableDeclaration parameterDeclaration : functionDeclaration.getParameters()) {
      if (first) {
        first = false;
      } else {
        writer.write(", ");
      }

      parameterDeclaration.accept(this, writer);
    }

    writer.write(") ");

    final Block body = functionDeclaration.getBody();
    body.accept(this, writer);

    writer.write("\n");

    this.globalDeclaration = wasGlobalDeclaration;
    return null;
  }

  @Override
  public final Void visit(final Block block, final SafeWriter writer) {
    writer.write("{\n");

    this.currentIndentation += 2;

    for (final Statement statement : block.getStatements()) {
      writeIndentation(writer);
      statement.accept(this, writer);
      writer.write("\n");
    }

    this.currentIndentation -= 2;
    writeIndentation(writer);

    writer.write("}");

    return null;
  }

  @Override
  public final Void visit(final TypeName typeName, final SafeWriter writer) {
    writer.write(typeName.toString());

    return null;
  }

  @Override
  public final Void visit(final AssignStatement assignStatement, final SafeWriter writer) {
    final Identifier leftHandSide = assignStatement.getLeftHandSide();
    leftHandSide.accept(this, writer);

    writer.write(" = ");

    final Expression rightHandSide = assignStatement.getRightHandSide();
    rightHandSide.accept(this, writer);

    writer.write(";");

    return null;
  }

  @Override
  public final Void visit(final FunctionCallStatement functionCallStatement,
      final SafeWriter writer) {
    final FunctionCall functionCall = functionCallStatement.getFunctionCall();
    functionCall.accept(this, writer);

    writer.write(";");

    return null;
  }

  @Override
  public final Void visit(final IfStatement ifStatement, final SafeWriter writer) {
    writer.write("if (");

    final Expression condition = ifStatement.getCondition();
    condition.accept(this, writer);

    writer.write(") ");

    final Block thenBlock = ifStatement.getThenBlock();
    thenBlock.accept(this, writer);

    if (ifStatement.hasElseBlock()) {
      writer.write(" else ");

      final Block elseBlock = ifStatement.getElseBlock();
      elseBlock.accept(this, writer);
    }

    return null;
  }

  @Override
  public final Void visit(final WhileLoop whileLoop, final SafeWriter writer) {
    writer.write("while (");

    final Expression condition = whileLoop.getCondition();
    condition.accept(this, writer);

    writer.write(") ");

    final Block body = whileLoop.getBody();
    body.accept(this, writer);

    return null;
  }

  @Override
  public final Void visit(final ReturnStatement returnStatement, final SafeWriter writer) {
    writer.write("return");

    if (returnStatement.hasReturnValue()) {
      writer.write(" ");

      final Expression returnValue = returnStatement.getReturnValue();
      returnValue.accept(this, writer);
    }

    writer.write(";");

    return null;
  }

  @Override
  public final Void visit(final DeclarationStatement declarationStatement,
      final SafeWriter writer) {
    final Declaration declaration = declarationStatement.getDeclaration();
    declaration.accept(this, writer);

    writer.write(";");

    return null;
  }

  @Override
  public final Void visit(final Identifier identifier, final SafeWriter writer) {
    writer.write(identifier.toString());

    return null;
  }

  @Override
  public final Void visit(final Literal literal, final SafeWriter writer) {
    writer.write(literal.toString());

    return null;
  }

  @Override
  public final Void visit(final BinaryExpression binaryExpression, final SafeWriter writer) {
    final Expression leftOperand = binaryExpression.getLeftHandSide();
    {
      final boolean parenthesesNeeded = parenthesesNeeded(binaryExpression, leftOperand);
      if (parenthesesNeeded) {
        writer.write("(");
      }

      leftOperand.accept(this, writer);

      if (parenthesesNeeded) {
        writer.write(")");
      }
    }

    writer.write(" %s ", binaryExpression.getOperator().stringRepresentation);

    final Expression rightOperand = binaryExpression.getRightHandSide();
    {
      final boolean parenthesesNeeded = parenthesesNeeded(binaryExpression, rightOperand);
      if (parenthesesNeeded) {
        writer.write("(");
      }

      rightOperand.accept(this, writer);

      if (parenthesesNeeded) {
        writer.write(")");
      }
    }

    return null;
  }

  @Override
  public final Void visit(final FunctionCall functionCall, final SafeWriter writer) {
    final Identifier callee = functionCall.getCallee();
    callee.accept(this, writer);

    writer.write("(");

    boolean first = true;
    for (final Expression argument : functionCall.getArguments()) {
      if (first) {
        first = false;
      } else {
        writer.write(", ");
      }

      argument.accept(this, writer);
    }

    writer.write(")");

    return null;
  }

}
