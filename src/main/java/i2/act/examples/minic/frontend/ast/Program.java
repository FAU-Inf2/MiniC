package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

import java.util.Collections;
import java.util.List;

public final class Program extends ASTNode {

  private final List<Declaration> declarations;

  public Program(final SourcePosition position, final List<Declaration> declarations) {
    super(position);
    this.declarations = declarations;
  }

  public final List<Declaration> getDeclarations() {
    return Collections.unmodifiableList(this.declarations);
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
