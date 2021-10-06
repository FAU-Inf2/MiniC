package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public final class DeclarationStatement extends Statement {

  private final Declaration declaration;

  public DeclarationStatement(final SourcePosition position, final Declaration declaration) {
    super(position);
    this.declaration = declaration;
  }

  public final Declaration getDeclaration() {
    return this.declaration;
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
