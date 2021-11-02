package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.lexer.Token;
import i2.act.examples.minic.frontend.semantics.symbols.Symbol;

public final class Identifier extends Expression {

  private final Token token;

  private Symbol symbol;

  public Identifier(final SourcePosition position, final Token token) {
    super(position);
    this.token = token;
  }

  public final Token getToken() {
    return this.token;
  }

  public final String getName() {
    return this.token.string;
  }

  public final void setSymbol(final Symbol symbol) {
    this.symbol = symbol;
  }

  public final Symbol getSymbol() {
    return this.symbol;
  }

  @Override
  public final String toString() {
    return this.token.string;
  }

  @Override
  public final boolean isTerminal() {
    return true;
  }

  @Override
  public final <P, R> R accept(final ASTVisitor<P, R> visitor, final P parameter) {
    return visitor.visit(this, parameter);
  }

}
