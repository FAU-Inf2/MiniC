package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public final class ReturnStatement extends Statement {

  private final Expression returnValue;

  public ReturnStatement(final SourcePosition position, final Expression returnValue) {
    super(position);
    this.returnValue = returnValue;
  }

  public final Expression getReturnValue() {
    return this.returnValue;
  }

  public final boolean hasReturnValue() {
    return this.returnValue != null;
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
