package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.info.SourcePosition;

public abstract class Statement extends ASTNode {

  public Statement(final SourcePosition position) {
    super(position);
  }

}
