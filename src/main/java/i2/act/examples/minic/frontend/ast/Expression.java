package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.semantics.types.Type;

public abstract class Expression extends ASTNode {

  protected Type type;

  public Expression(final SourcePosition position) {
    super(position);
  }

  public final void setType(final Type type) {
    this.type = type;
  }

  public final Type getType() {
    return this.type;
  }

}
