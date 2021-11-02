package i2.act.examples.minic.frontend.ast.visitors;

import i2.act.examples.minic.frontend.ast.*;

public interface ASTVisitor<P, R> {

  public R visit(final Program program, final P parameter);

  public R visit(final VariableDeclaration variableDeclaration, final P parameter);

  public R visit(final FunctionDeclaration functionDeclaration, final P parameter);

  public R visit(final Block block, final P parameter);

  public R visit(final TypeName typeName, final P parameter);

  public R visit(final AssignStatement assignStatement, final P parameter);

  public R visit(final FunctionCallStatement functionCallStatement, final P parameter);

  public R visit(final IfStatement ifStatement, final P parameter);

  public R visit(final WhileLoop whileLoop, final P parameter);

  public R visit(final ReturnStatement returnStatement, final P parameter);

  public R visit(final DeclarationStatement declarationStatement, final P parameter);

  public R visit(final Identifier identifier, final P parameter);

  public R visit(final Literal literal, final P parameter);

  public R visit(final BinaryExpression binaryExpression, final P parameter);

  public R visit(final FunctionCall functionCall, final P parameter);

}
