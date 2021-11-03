package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

import java.util.Collections;
import java.util.List;

public final class Block extends Statement {

  private final List<Statement> statements;

  public Block(final SourcePosition position, final List<Statement> statements) {
    super(position);
    this.statements = statements;
  }

  public final List<Statement> getStatements() {
    return Collections.unmodifiableList(this.statements);
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
