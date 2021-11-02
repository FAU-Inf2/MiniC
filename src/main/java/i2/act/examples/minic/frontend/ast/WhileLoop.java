package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public final class WhileLoop extends Statement {

  private final Expression condition;
  private final Block body;

  public WhileLoop(final SourcePosition position, final Expression condition, final Block body) {
    super(position);
    this.condition = condition;
    this.body = body;
  }

  public final Expression getCondition() {
    return this.condition;
  }

  public final Block getBody() {
    return this.body;
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
