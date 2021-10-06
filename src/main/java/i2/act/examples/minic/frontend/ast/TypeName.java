package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.lexer.Token;
import i2.act.examples.minic.frontend.semantics.types.Type;

public final class TypeName extends ASTNode {

  private final Token typeNameToken;

  private Type type;

  public TypeName(final SourcePosition position, final Token typeNameToken) {
    super(position);
    this.typeNameToken = typeNameToken;
  }

  public final Token getTypeNameToken() {
    return this.typeNameToken;
  }

  public final void setType(final Type type) {
    this.type = type;
  }

  public final Type getType() {
    return this.type;
  }

  @Override
  public final String toString() {
    return this.typeNameToken.kind.stringRepresentation;
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
