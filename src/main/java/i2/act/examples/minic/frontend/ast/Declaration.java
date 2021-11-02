package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.semantics.symbols.Symbol;
import i2.act.examples.minic.frontend.semantics.types.Type;

public abstract class Declaration extends ASTNode {

  public Declaration(final SourcePosition position) {
    super(position);
  }

  public abstract Symbol getSymbol();

  public abstract Type getType();

}
