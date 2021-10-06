package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public final class AssignStatement extends Statement {

  private final Identifier leftHandSide;
  private final Expression rightHandSide;

  public AssignStatement(final SourcePosition position, final Identifier leftHandSide,
      final Expression rightHandSide) {
    super(position);
    this.leftHandSide = leftHandSide;
    this.rightHandSide = rightHandSide;
  }

  public final Identifier getLeftHandSide() {
    return this.leftHandSide;
  }

  public final Expression getRightHandSide() {
    return this.rightHandSide;
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
