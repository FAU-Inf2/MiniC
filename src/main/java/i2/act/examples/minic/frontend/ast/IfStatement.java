package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public final class IfStatement extends Statement {

  private final Expression condition;
  private final Block thenBlock;
  private final Block elseBlock;

  public IfStatement(final SourcePosition position, final Expression condition,
      final Block thenBlock) {
    this(position, condition, thenBlock, null);
  }

  public IfStatement(final SourcePosition position, final Expression condition,
      final Block thenBlock, final Block elseBlock) {
    super(position);
    this.condition = condition;
    this.thenBlock = thenBlock;
    this.elseBlock = elseBlock;
  }

  public final Expression getCondition() {
    return this.condition;
  }

  public final Block getThenBlock() {
    return this.thenBlock;
  }

  public final Block getElseBlock() {
    return this.elseBlock;
  }

  public final boolean hasElseBlock() {
    return this.elseBlock != null;
  }

  @Override
  public final boolean isTerminal() {
    return false;
  }

  @Override
  public final <P, R> R accept(final ASTVisitor<P, R> visitor, final P parameter) {
    return visitor.visit(this, parameter);
  }

}
