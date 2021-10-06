package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public abstract class ASTNode {

  protected final SourcePosition position;

  public ASTNode(final SourcePosition position) {
    this.position = position;
  }

  public final SourcePosition getPosition() {
    return this.position;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public abstract boolean isTerminal();

  public abstract <P, R> R accept(final ASTVisitor<P, R> visitor, final P parameter);

}
